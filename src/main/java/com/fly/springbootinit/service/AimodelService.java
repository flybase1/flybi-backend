package com.fly.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fly.springbootinit.model.dto.aimodel.AiModelAddRequest;
import com.fly.springbootinit.model.dto.aimodel.AiModelQueryRequest;
import com.fly.springbootinit.model.dto.user.UserQueryRequest;
import com.fly.springbootinit.model.entity.Aimodel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.springbootinit.model.entity.User;

/**
 * @author admin
 * @description 针对表【aimodel】的数据库操作Service
 * @createDate 2023-07-05 17:59:27
 */
public interface AimodelService extends IService<Aimodel> {

    /**
     * 添加AI模型
     * @param aiModelAddRequest
     * @return
     */
    Aimodel AddAiModel(AiModelAddRequest aiModelAddRequest);


    QueryWrapper<Aimodel> getQueryWrapper(AiModelQueryRequest aiModelQueryRequest);

}
