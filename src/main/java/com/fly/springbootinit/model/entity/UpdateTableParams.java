package com.fly.springbootinit.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 添加表结构
 */
@Data
public class UpdateTableParams {
    private String tableName;
    private List<String> columnNames;

}