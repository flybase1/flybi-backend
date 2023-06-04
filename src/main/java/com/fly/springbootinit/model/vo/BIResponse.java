package com.fly.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * BI返回结果
 */
@Data
public class BIResponse implements Serializable {

    private static final long serialVersionUID = -1597248846983020341L;
    private String genChart;

    private String genResult;

    private Long chartId;
}
