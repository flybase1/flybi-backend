package com.fly.springbootinit.mapper;

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


    /**
     * 删除表
     *
     * @param tableName
     * @return
     */
    boolean deleteChartDetail(String tableName);

    /**
     * 更具条件查询语句
     *
     * @param paramMap
     * @return
     */
    List<Map<String, Object>> findDataByCondition(Map<String, Object> paramMap);


    /**
     * 修改表名
     *
     * @return
     */
    boolean updateTableName(@Param( "tableName" ) String oldTableName, @Param( "newTableName" ) String newTableName);

    /**
     * 修改表属性
     */
    void updateTableColumns(Map<String, Object> paramMap);
}
