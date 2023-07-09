package com.fly.springbootinit.mapper;

import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

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
        String chartTableName = "chart_" + 1676479689666863106L;
        List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartTableName);
        System.out.println(allData);
        //List<Map<String, Object>> allData = chartDetailMapper.findAllData(chartTableName);
        //allData.forEach(System.out::println);
    }


    @Test
    void deleteDetailTable() {
        String tableName = "chart_" + 1675788619610107906L;
        chartDetailMapper.deleteChartDetail(tableName);

    }


    @Test
    void selectByCondition() {
        Map<String,Object> map =new HashMap<>();
        List<String> list =new ArrayList<>();
        list.add("日期");
        list.add("人数");
        map.put("tableName","chart_1676759495763673089");
        map.put("columnNames",list);
        List<Map<String, Object>> dataByCondition = chartDetailMapper.findDataByCondition(map);
        System.out.println(dataByCondition);
    }
}