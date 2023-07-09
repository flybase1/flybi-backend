package com.fly.springbootinit.utils;

import com.fly.springbootinit.mapper.ChartDetailMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CSVUtilsTest {
    @Resource
    private ChartDetailMapper chartDetailMapper;

    @Test
    public void testTableToCSV() {
        String tableName = "chart_" + 1676479689666863106L;
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(tableName);
        String scvString = CSVUtils.getCSVString(allData);
        System.out.println(scvString);


    }
}