package com.fly.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartDetailMapperTest {
    @Resource
    private ChartDetailMapper chartDetailMapper;

    @Test
    void testCreateTable() {
        String chartTableName = "chart_" + 100080L;
        List<Map<String, Object>> columns = new ArrayList<>();
        Map<String, Object> column1 = new HashMap<>();
        column1.put("name", "name");
        column1.put("type", "varchar(100)");
        columns.add(column1);

        Map<String, Object> column2 = new HashMap<>();
        column2.put("name", "age");
        column2.put("type", "varchar(100)");
        columns.add(column2);
        //chartDetailMapper.createTable(chartTableName, columns);
    }


    @Test
    void testInsertData() {
        String chartTableName = "chart_" + 100080L;
        List<String> columnNames = Arrays.asList("name", "age");
        List<Object> values = Arrays.asList("w33z", "129");
        boolean success = chartDetailMapper.insertData(chartTableName, columnNames, values);
        System.out.println(success);
    }

    @Test
    void findAllData() {
        String chartTableName = "chart_" + 100080L;
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartTableName);
        allData.forEach(System.out::println);
    }
}