package com.fly.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ChartMapperTest {
    @Resource
    private ChartMapper chartMapper;


    @Test
    void name() {
        String chartId = "1664114814890311682";
        String querySql = String.format("select * from chart_%s",chartId);

        List<Map<String, Object>> maps = chartMapper.queryChartData(querySql);
        System.out.println(maps);
    }


    @Test
    void testShow() {
        Map<String, Object> map = chartMapper.selectLatestChart();
        System.out.println(map);
    }
}