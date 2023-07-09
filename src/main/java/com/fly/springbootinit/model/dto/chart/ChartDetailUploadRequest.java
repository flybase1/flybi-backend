package com.fly.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 这里用于自定义表格数据上传内容
 */
@Data
public class ChartDetailUploadRequest implements Serializable {
    /**
     * csv数据
     */
    private String csvData;
    /**
     * 目标
     */
    private String goal;
    /**
     * 表格类型
     */
    private String chartType;
    /**
     * 图表名字
     */
    private String name;

    private String chartDetailName;

    private static final long serialVersionUID = -7111113759186125480L;
}
