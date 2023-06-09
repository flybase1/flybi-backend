package com.fly.springbootinit.controller;

import java.time.LocalDateTime;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fly.springbootinit.annotation.AuthCheck;
import com.fly.springbootinit.api.YuApi;
import com.fly.springbootinit.bizmq.BIMessageProducer;
import com.fly.springbootinit.common.BaseResponse;
import com.fly.springbootinit.common.DeleteRequest;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.common.ResultUtils;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.constant.RedisConstant;
import com.fly.springbootinit.constant.UserConstant;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.exception.ThrowUtils;
import com.fly.springbootinit.manager.RedisLimiterManager;
import com.fly.springbootinit.mapper.ChartDetailMapper;
import com.fly.springbootinit.model.dto.chart.*;
import com.fly.springbootinit.model.entity.Chart;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.model.enums.ChartStatusEnum;
import com.fly.springbootinit.model.vo.BIResponse;
import com.fly.springbootinit.service.ChartService;
import com.fly.springbootinit.service.UserService;
import com.fly.springbootinit.utils.CSVUtils;
import com.fly.springbootinit.utils.ExcelUtils;
import com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 帖子接口
 */
@RestController
@RequestMapping( "/chart" )
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    @Resource
    private YuApi yuApi;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BIMessageProducer biMessageProducer;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChartDetailMapper chartDetailMapper;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping( "/add" )
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping( "/delete" )
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart chart = chartService.getById(id);
        ThrowUtils.throwIf(chart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!chart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        String tableName = "chart_" + id;
        if (b) {
            // 删除数据的同时删除相关的表
            boolean deleteChartDetail = chartDetailMapper.deleteChartDetail(tableName);
            if (!deleteChartDetail) {
                return ResultUtils.success(b);
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
            }
        }

        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping( "/update" )
    @AuthCheck( mustRole = UserConstant.ADMIN_ROLE )
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        Chart chart = chartService.getById(chartUpdateRequest.getId());
        ThrowUtils.throwIf(chart == null, ErrorCode.NOT_FOUND_ERROR);
        Chart newChart = new Chart();
        newChart.setId(chart.getId());
        if (chartUpdateRequest.getFailedCount() > 2 || chartUpdateRequest.getFailedCount() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能大于2或者小于0");
        }
        newChart.setFailedCount(chartUpdateRequest.getFailedCount());
        newChart.setStatus(chartUpdateRequest.getStatus());
        boolean result = chartService.updateById(newChart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping( "/get" )
    public BaseResponse<Chart> getChartById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 获取当前详细的数据库表
     *
     * @param id
     * @return
     */
    @GetMapping( "/chartDetail" )
    public BaseResponse<List<Map<String, Object>>> getChartDetailById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String chartName = "chart_" + id;
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartName);

        return ResultUtils.success(allData);
    }


    /**
     * 分页获取列表（封装类）
     * 管理员
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @AuthCheck( mustRole = UserConstant.ADMIN_ROLE )
    @PostMapping( "/list/page" )
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     * todo 增加redis缓存
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping( "/my/list/page" )
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
            if (chartQueryRequest == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User loginUser = userService.getLoginUser(request);
            chartQueryRequest.setUserId(loginUser.getId());
            long current = chartQueryRequest.getCurrent();
            long size = chartQueryRequest.getPageSize();
            chartQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
            // 限制爬虫
            ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

            Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                    chartService.getQueryWrapper(chartQueryRequest));

            return ResultUtils.success(chartPage);
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping( "/edit" )
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        // 参数校验
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart newChart = chartService.getById(id);
        ThrowUtils.throwIf(newChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!newChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(newChart);
        return ResultUtils.success(result);
    }


    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping( "/gen" )
    public BaseResponse<BIResponse> genChartByAi(@RequestPart( "file" ) MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAiRequest, HttpServletRequest request) throws InterruptedException {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xls", "xlsx", "csv");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // 无需写 prompt，直接调用现有模型，https://www.yucongming.com，公众号搜【鱼聪明AI】
//        final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
//                "分析需求：\n" +
//                "{数据分析的需求或者目标}\n" +
//                "原始数据：\n" +
//                "{csv格式的原始数据，用,作为分隔符}\n" +
//                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
//                "【【【【【\n" +
//                "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
//                "【【【【【\n" +
//                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
        long biModelId = 1659171950288818178L;
        // 分析需求：
        // 分析网站用户的增长情况
        // 原始数据：
        // 日期,用户数
        // 1号,10
        // 2号,20
        // 3号,30

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        String result = yuApi.doChart(biModelId, userInput.toString());
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        //chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        Thread.sleep(1000);
        //chart.setStatus(ChartStatusEnum.Succeed.getValue());
        chart.setUserId(loginUser.getId());
        boolean countSuccess = userService.updateUserChartCount(request);

        if (!countSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        boolean saveResult = chartService.save(chart);
        if (!saveResult) {
            chart.setStatus(ChartStatusEnum.Failed.getValue());
        }

        BIResponse biResponse = new BIResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        createAndInsertDataToDb(chart.getId(), multipartFile);
        updateTableNameAndChartStatus(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 由于同步的时候没有chartId，所以只能提取方法来转换chart状态
     *
     * @param chartId
     */
    private void updateTableNameAndChartStatus(Long chartId) {
        Chart chart = chartService.getById(chartId);
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setChartDetailTableName("chart_" + chartId);
        if (chart.getGenChart() == null || chart.getGenResult() == null) {
            chart.setStatus(ChartStatusEnum.Failed.getValue());
        }
        updateChart.setStatus(ChartStatusEnum.Succeed.getValue());
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


    /**
     * 智能分析 异步
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping( "/gen/async" )
    public BaseResponse<BIResponse> genChartAiAsync(@RequestPart( "file" ) MultipartFile multipartFile,
                                                    GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();

        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "请求为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() >= 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //todo 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件过大，超过1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls", "csv");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // todo 限流判断
        // 每个用户限流器
        redisLimiterManager.doRateLimit("genChartByAi" + loginUser.getId());

        // 系统预设,直接调用，不需要自己使用预设词
/*        final String prompt="你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";*/
        long modelId = 1663789163637420033L;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");

        // 文件处理,压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        stringBuilder.append(result).append("\n");

        // 先插入数据库
        // 插入数据库
        Chart chart = new Chart();
        chart.setChartType(chartType);
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(result);
        chart.setStatus(ChartStatusEnum.Wait.getValue());
        chart.setUserId(userService.getLoginUser(request).getId());

        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存失败");
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());

        // todo 采用异步
        // todo 建议处理异常情况
        try {
            CompletableFuture.runAsync(() -> {
                //先修改图表状态为'执行中'，执行完毕后修改为'已完成'，保存执行结果，执行失败后，状态修改为'失败'记录失败信息
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus(ChartStatusEnum.Running.getValue());
                boolean success = chartService.updateById(updateChart);
                if (!success) {
                    // todo 进一步完善失败
                    handleChartUpdateError(chart.getId(), "更新图表状态失败");
                    // throw new BusinessException(ErrorCode.OPERATION_ERROR, "图表更新失败");
                    return;
                }

                // 调用AI
                String chartResult = yuApi.doChart(modelId, stringBuilder.toString());
                String[] splits = chartResult.split("【【【【【");

                if (splits.length > 3) {
                    handleChartUpdateError(chart.getId(), "AI生成错误");
                    // throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
                    return;
                }
                String genChart = splits[1].trim();
                String genResult = splits[2].trim();
                Chart updateChartSuccess = new Chart();
                updateChartSuccess.setId(chart.getId());
                // todo 枚举值
                updateChartSuccess.setStatus(ChartStatusEnum.Succeed.getValue());
                updateChartSuccess.setGenChart(genChart);
                updateChartSuccess.setGenResult(genResult);
                boolean b = chartService.updateById(updateChartSuccess);
                if (!b) {
                    handleChartUpdateError(chart.getId(), "更新图表状态失败");
                }
            }, threadPoolExecutor);
        } catch (Exception e) {
            //todo 添加重试机制
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同时请求人数过多，稍后进行尝试");
        }
        return ResultUtils.success(biResponse);
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartSuccess = new Chart();
        updateChartSuccess.setId(chartId);
        updateChartSuccess.setStatus(ChartStatusEnum.Failed.getValue());
        updateChartSuccess.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartSuccess);
        if (!b) {
            log.error(chartId + ": 更新失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图表更新失败");
        }
    }


    /**
     * 使用消息队列RabbitMq
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping( "/gen/async/mq" )
    public BaseResponse<BIResponse> genChartAiAsyncMQ(@RequestPart( "file" ) MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "请求为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() >= 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //todo 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件过大，超过1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls", "csv");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // todo 限流判断
        // 每个用户限流器
        redisLimiterManager.doRateLimit("genChartByAi" + loginUser.getId());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");

        // 文件处理,压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        stringBuilder.append(result).append("\n");

        // 先插入数据库
        Chart chart = new Chart();
        chart.setChartType(chartType);
        chart.setName(name);
        chart.setGoal(goal);

        //这个原数据不存在在这里，而是在其他表里面
        //chart.setChartData(result);
        chart.setStatus(ChartStatusEnum.Wait.getValue());
        chart.setUserId(userService.getLoginUser(request).getId());
        boolean countSuccess = userService.updateUserChartCount(request);

        if (!countSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存失败");
        BIResponse biResponse = new BIResponse();
        Long chartId = chart.getId();
        biResponse.setChartId(chartId);

        createAndInsertDataToDb(chartId, multipartFile);

        // todo 采用异步
        // todo 建议处理任务队列满了异常情况
        try {
            biMessageProducer.sendMessage(String.valueOf(chartId));
        } catch (Exception e) {
            //todo 添加重试机制
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同时请求人数过多，稍后进行尝试");
        }

        return ResultUtils.success(biResponse);
    }

    /**
     * 分库
     *
     * @param chartId
     * @param multipartFile
     */
    private void createAndInsertDataToDb(Long chartId, MultipartFile multipartFile) {
        // 动态sql分库存储表数据
        String chartDetailName = "chart_" + chartId;
        //获取表头
        String csvHeader = ExcelUtils.excelToCsvGetHeader(multipartFile);
        //log.error("=============================>" + csvHeader);
        String[] split = csvHeader.split(",");
        List<String> headers = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        for (String s : split) {
            if (containsChinese(s)) {
                headers.add("'" + s + "'" + " varchar(1024)");
            } else {
                headers.add(s + " varchar(1024)");
            }
            columnNames.add(s);
        }

        chartDetailMapper.createTable(chartDetailName, headers);

        // 获取数据
        String content = ExcelUtils.excelToCsvGetContent(multipartFile);
        // log.error("内容=>" + content);
        String[] contentValues = content.split("\n");
        for (String row : contentValues) {
            String[] rowValues = row.split(",");
            List<Object> values = Arrays.asList(rowValues);
            boolean saveSuccess = chartDetailMapper.insertData(chartDetailName, columnNames, values);
            if (!saveSuccess) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }

    }

    /**
     * 判断是否为中文
     *
     * @param str
     * @return
     */
    private static boolean containsChinese(String str) {
        String regex = "[\\u4e00-\\u9fa5]";
        return str.matches(regex);
    }


    /**
     * 手动重试，重试次数有三次
     * 重试,假设失败的原因是ai服务传递结果不正确，已经创建了正确的数据库
     * 将相应的数据从数据库中取出，读取数据库相应的表的内容，转换为csv格式，模拟上传
     * 采用异步化来完成调用
     *
     * @param request
     * @return
     */
    @PostMapping( "/gen/async/asyn/retry" )
    public BaseResponse<BIResponse> genChartAiAsyncRetry(GenChartByAIIDRequest genChartByAIIDRequest, HttpServletRequest request) {
        Long chartId = genChartByAIIDRequest.getId();
        Chart chartRetry = chartService.getById(chartId);

        if (chartRetry.getFailedCount() >= 3) {
            chartRetry.setStatus(ChartStatusEnum.Failed.getValue());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "重试次数已达三次，第三方服务宕机，请联系管理员");
        }

        String goal = chartRetry.getGoal();
        String chartType = chartRetry.getChartType();
        String chartDetailTableName = chartRetry.getChartDetailTableName();

        if (chartRetry == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        chartRetry.setGenChart("");
        chartRetry.setGenResult("");
        boolean b = chartService.updateById(chartRetry);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        // todo 限流判断
        // 每个用户限流器
        redisLimiterManager.doRateLimit("genChartByAi" + loginUser.getId());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");

        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartDetailTableName);
        String data = CSVUtils.getCSVString(allData);
        stringBuilder.append(data).append("\n");

        // 调用次数减少
        boolean countSuccess = userService.updateUserChartCount(request);
        if (!countSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chartId);

        try {
            CompletableFuture.runAsync(() -> {
                //先修改图表状态为'执行中'，执行完毕后修改为'已完成'，保存执行结果，执行失败后，状态修改为'失败'记录失败信息
                Chart updateChart = new Chart();
                updateChart.setId(chartId);
                updateChart.setStatus(ChartStatusEnum.Running.getValue());
                // 重试次数加一
                updateChart.setFailedCount(chartRetry.getFailedCount() + 1);
                boolean success = chartService.updateById(updateChart);
                if (!success) {
                    // todo 进一步完善失败
                    handleChartUpdateError(chartId, "更新图表状态失败");
                    // throw new BusinessException(ErrorCode.OPERATION_ERROR, "图表更新失败");
                    return;
                }

                // 调用AI
                String chartResult = yuApi.doChart(CommonConstant.BI_MODEL_ID, stringBuilder.toString());
                String[] splits = chartResult.split("【【【【【");

                if (splits.length > 3) {
                    handleChartUpdateError(chartId, "AI生成错误");
                    // throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
                    return;
                }
                String genChart = splits[1].trim();
                String genResult = splits[2].trim();
                //todo 去除无关的内容
                int startIndex = genChart.indexOf("{");
                int lastIndex = genChart.lastIndexOf("}");
                if (startIndex != -1 && lastIndex != -1 && lastIndex > startIndex) {
                    genChart = genChart.substring(startIndex, lastIndex + 1).trim();
                } else {
                    log.error("split genChart error" + LocalDateTime.now());
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                Chart updateChartSuccess = new Chart();
                updateChartSuccess.setId(chartId);
                // todo 枚举值
                updateChartSuccess.setStatus(ChartStatusEnum.Succeed.getValue());
                updateChartSuccess.setGenChart(genChart);
                updateChartSuccess.setGenResult(genResult);
                boolean successU = chartService.updateById(updateChartSuccess);
                if (!successU) {
                    handleChartUpdateError(chartId, "更新图表状态失败");
                }
            }, threadPoolExecutor);
        } catch (Exception e) {
            log.error("function genChartAiAsyncRetry error" + LocalDateTime.now());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统同时请求人数过多，稍后再试");
        }

        return ResultUtils.success(biResponse);

    }

}
