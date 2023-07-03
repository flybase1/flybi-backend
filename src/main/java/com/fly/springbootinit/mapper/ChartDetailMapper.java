package com.fly.springbootinit.mapper;

import com.fly.springbootinit.model.entity.Chart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChartDetailMapper {
    /**
     * 创建表
     *
     * @param tableName
     */
    boolean createTable(@Param( "tableName" ) String tableName, @Param( "columns" ) List<String> columns);

    /**
     * 插入数据
     *
     * @param tableName
     * @param columnNames
     * @param values
     */
    boolean insertData(@Param( "tableName" ) String tableName, @Param( "columnNames" ) List<String> columnNames, @Param( "values" ) List<Object> values);

    /**
     * 查看所有数据
     *
     * @param tableName
     * @return
     */
    List<Map<String, Object>> findAllData(String tableName);

}
