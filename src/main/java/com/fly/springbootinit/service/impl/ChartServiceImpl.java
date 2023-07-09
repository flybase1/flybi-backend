package com.fly.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.mapper.ChartMapper;
import com.fly.springbootinit.model.dto.chart.ChartQueryRequest;

import com.fly.springbootinit.model.entity.Chart;
import com.fly.springbootinit.model.enums.ChartStatusEnum;
import com.fly.springbootinit.service.ChartService;

import com.fly.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-05-30 19:03:59
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(ObjectUtils.isNotEmpty(name), "name", name);
        queryWrapper.ne(ObjectUtils.isNotEmpty(goal), "goal", goal);
        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public boolean ChangeLongTimeWaitToFailed() {
        List<Chart> list = this.list();
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration waitDurationThreshold = Duration.ofMinutes(10);

        List<Chart> collect = list.stream()
                .filter(chart -> !chart.getStatus().equals(ChartStatusEnum.Failed.getValue()) && !chart.getStatus().equals(ChartStatusEnum.Succeed.getValue()))
                .filter(chart -> Duration.between(chart.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), currentDateTime).compareTo(waitDurationThreshold) >= 0)
                .map(chart -> {
                    Chart newChart = new Chart();
                    newChart.setId(chart.getId());
                    newChart.setGoal(chart.getGoal());
                    newChart.setName(chart.getName());
                    newChart.setChartData(chart.getChartData());
                    newChart.setChartType(chart.getChartType());
                    newChart.setGenChart(chart.getGenChart());
                    newChart.setGenResult(chart.getGenResult());
                    newChart.setUserId(chart.getUserId());
                    newChart.setStatus(ChartStatusEnum.Failed.getValue());
                    newChart.setExecMessage(chart.getExecMessage());
                    return newChart;
                }).collect(Collectors.toList());
        return this.updateBatchById(collect);
    }

}




