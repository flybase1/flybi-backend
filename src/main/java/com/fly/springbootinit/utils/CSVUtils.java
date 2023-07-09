package com.fly.springbootinit.utils;

import com.fly.springbootinit.mapper.ChartDetailMapper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将数据库表以及数据读取到csv格式的String里面
 */
@Slf4j
public class CSVUtils {

    /**
     * 主题思路：先读取数据库文件，然后第一行取出键，这就是列名，然后依次通过列名获取相应列行的数据
     * @param dataList
     * @return
     */
    public static String getCSVString(List<Map<String, Object>> dataList) {
        StringBuilder csvString = new StringBuilder();
        Map<String, Object> firstRow = dataList.get(0);
        String[] columnNames = firstRow.keySet().toArray(new String[0]);
        for (String columnName : columnNames) {
            csvString.append(columnName).append(",");
        }
        // 删除最后一个逗号
        csvString.deleteCharAt(csvString.length() - 1);
        csvString.append("\n");
        for (Map<String, Object> columns : dataList) {
            for (String columnName : columnNames) {
                Object value = columns.get(columnName);
                csvString.append(value).append(",");
            }
            csvString.deleteCharAt(csvString.length() - 1);
            csvString.append("\n");
        }

        return csvString.toString();
    }

}
