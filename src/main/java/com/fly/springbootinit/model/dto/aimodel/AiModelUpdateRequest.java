package com.fly.springbootinit.model.dto.aimodel;

import lombok.Data;

import java.io.Serializable;

/**
 * @TableName aimodel
 */

@Data
public class AiModelUpdateRequest implements Serializable {

    private Integer id;
    /**
     * 创建ai名字
     */
    private String AIName;

    /**
     * 描述AI
     */
    private String AIDescription;

    /**
     * AI的路由
     */
    private String AIRoute;

    /**
     * 是否上线,online/offline
     */
    private String isOnline;

    private String AIAvatar;


    private static final long serialVersionUID = 1L;


}