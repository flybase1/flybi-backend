package com.fly.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.springbootinit.model.entity.Chart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
* @author admin
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-05-30 19:03:59
* @Entity com.fly.springbootinit.model.entity.Chart
*/
@Mapper
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String,Object>> queryChartData(String querySql);
}




