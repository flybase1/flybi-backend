package com.fly.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.fly.springbootinit.bizmq.BIMessageProducer;
import com.fly.springbootinit.common.BaseResponse;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.common.ResultUtils;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.exception.ThrowUtils;
import com.fly.springbootinit.manager.RedisLimiterManager;
import com.fly.springbootinit.mapper.ChartDetailMapper;
import com.fly.springbootinit.model.dto.chart.ChartDetailUploadRequest;
import com.fly.springbootinit.model.entity.Chart;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.model.enums.ChartStatusEnum;
import com.fly.springbootinit.model.vo.BIResponse;
import com.fly.springbootinit.service.ChartService;
import com.fly.springbootinit.service.UserService;
import com.fly.springbootinit.utils.CSVUtils;
import com.fly.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping( "/chartDetail" )
@Slf4j
public class ChartDetailController {
    @Resource
    private ChartDetailMapper chartDetailMapper;
    @Resource
    private UserService userService;
    @Resource
    private ChartService chartService;
    @Resource
    private BIMessageProducer biMessageProducer;
    @Resource
    private RedisLimiterManager redisLimiterManager;

    /**
     * 用户先上传表，然后再分析
     *
     * @param multipartFile
     * @return
     */
    @PostMapping( "/upload" )
    public BaseResponse<Map<String, Object>> uploadExcelAndCreateTable(@RequestPart( "file" ) MultipartFile multipartFile) {
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件过大，超过1MB");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls", "csv");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        String chartDetailTable = createAndInsertDataToDb(multipartFile);
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartDetailTable);
        Map<String, Object> response = new HashMap<>();
        response.put("tableName", chartDetailTable);
        response.put("data", allData);
        return ResultUtils.success(response);
    }

    /**
     * 将用户选择的属性以及相应的数据进行传递保存为csv格式
     *
     * @param paramMap
     * @return
     */
    @PostMapping( "/chooseDeatail" )
    public BaseResponse<String> chooseUploadExcelStats(@RequestBody Map<String, Object> paramMap) {
        String tableName = (String) paramMap.get("tableName"); // 获取表名
        List<String> columnNames = (List<String>) paramMap.get("columnNames");
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableName);
        map.put("columnNames", columnNames);
        List<Map<String, Object>> result = chartDetailMapper.findDataByCondition(map);
        String csvString = CSVUtils.getCSVString(result);
        return ResultUtils.success(csvString);
    }


    /**
     * 将先传入的数据提取出来，然后再进行提问
     *
     * @param chartDetailUploadRequest
     * @param request
     * @return
     */
    @PostMapping( "/chartUpload" )
    public BaseResponse<BIResponse> genChart(@RequestBody ChartDetailUploadRequest chartDetailUploadRequest, HttpServletRequest request) {
        if (chartDetailUploadRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 获取传递数据
        String csvData = chartDetailUploadRequest.getCsvData();
        String goal = chartDetailUploadRequest.getGoal();
        String chartType = chartDetailUploadRequest.getChartType();
        String name = chartDetailUploadRequest.getName();
        String chartDetailName = chartDetailUploadRequest.getChartDetailName();

        // 拼接需求
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");
        stringBuilder.append(csvData).append("\n");

        //先插入数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setChartType(chartType);
        //chart.setChartData(csvData);
        chart.setStatus(ChartStatusEnum.Wait.getValue());
        //chart.setChartDetailTableName(chartDetailName);

        boolean save = chartService.save(chart);
        // 是否保存成功
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 减少次数
        boolean b = userService.updateUserChartCount(request);
        if (!b) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求失败");
        }
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        chartDetailMapper.updateTableName(chartDetailName, "chart_" + chart.getId());
        redisLimiterManager.doRateLimit("genChartByAi" + loginUser.getId());
        try {
            biMessageProducer.sendMessage(String.valueOf(chart.getId()));
        } catch (Exception e) {
            //todo 添加重试机制
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同时请求人数过多，稍后进行尝试");
        }

        return ResultUtils.success(biResponse);

    }


    /**
     * 插入数据库
     *
     * @param multipartFile
     */
    private String createAndInsertDataToDb(MultipartFile multipartFile) {
        // 动态sql分库存储表数据
        String chartDetailName = "chartDetail_" + RandomUtil.randomNumbers(7);
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
        return chartDetailName;
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
}
