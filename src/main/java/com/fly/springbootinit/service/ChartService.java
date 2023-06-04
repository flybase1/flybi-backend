package com.fly.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fly.springbootinit.model.dto.chart.ChartQueryRequest;
import com.fly.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-05-30 19:03:59
*/
public interface ChartService extends IService<Chart> {
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) ;
}
