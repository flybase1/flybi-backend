package com.fly.springbootinit.websocket;

import lombok.Data;

import java.util.Date;

@Data
public class ChartLeatest {
    private String status;
    private String name;
    private Long id;
    private Date updateTime;
}
