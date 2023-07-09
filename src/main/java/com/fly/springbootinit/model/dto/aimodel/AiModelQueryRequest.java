package com.fly.springbootinit.model.dto.aimodel;

import com.fly.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;

@EqualsAndHashCode( callSuper = true )
@Data
public class AiModelQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
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
