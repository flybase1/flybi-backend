package com.fly.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAIRequest implements Serializable {

    private String name;

    private String goal;

    private String chartType;


    private static final long serialVersionUID = -584938298147390124L;
}
