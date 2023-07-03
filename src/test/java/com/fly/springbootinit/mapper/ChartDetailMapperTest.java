package com.fly.springbootinit.mapper;

import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ChartDetailMapperTest {
    @Resource
    private ChartDetailMapper chartDetailMapper;
    @Resource
    private UserService userService;
    @Test
    void testCreateTable() {
        String chartTableName = "chart_" + 100080L;
        List<String> columns = new ArrayList<>();
        columns.add("name varchar(1024)");
        columns.add("age varchar(100)");
        chartDetailMapper.createTable(chartTableName, columns);
    }


    @Test
    void testInsertData() {
        String chartTableName = "chart_" + 100080L;
        List<String> columnNames = Arrays.asList("name", "age");
        List<Object> values = Arrays.asList("w33z", "19");
        boolean success = chartDetailMapper.insertData(chartTableName, columnNames, values);
        System.out.println(success);
    }

    @Test
    void findAllData() {
        String chartTableName = "chart_" + 1675338512024469506L;
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartTableName);
        System.out.println(allData);
        //List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartTableName);
        //allData.forEach(System.out::println);
    }

}