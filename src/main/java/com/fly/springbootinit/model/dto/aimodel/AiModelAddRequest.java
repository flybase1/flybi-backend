package com.fly.springbootinit.model.dto.aimodel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName aimodel
 */

@Data
public class AiModelAddRequest implements Serializable {

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