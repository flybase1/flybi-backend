package com.fly.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.model.dto.aimodel.AiModelAddRequest;
import com.fly.springbootinit.model.dto.aimodel.AiModelQueryRequest;
import com.fly.springbootinit.model.entity.Aimodel;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.AimodelService;
import com.fly.springbootinit.mapper.AimodelMapper;
import com.fly.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author admin
 * @description 针对表【aimodel】的数据库操作Service实现
 * @createDate 2023-07-05 17:59:27
 */
@Service
public class AimodelServiceImpl extends ServiceImpl<AimodelMapper, Aimodel>
        implements AimodelService {

    @Override
    public Aimodel AddAiModel(AiModelAddRequest aiModelAddRequest) {
        if (aiModelAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "信息填写完整");
        }
        Aimodel aimodel = new Aimodel();
        BeanUtils.copyProperties(aiModelAddRequest, aimodel);
        boolean save = this.save(aimodel);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return aimodel;
    }

    @Override
    public QueryWrapper<Aimodel> getQueryWrapper(AiModelQueryRequest aiModelQueryRequest) {
        if (aiModelQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = aiModelQueryRequest.getId();
        String aiName = aiModelQueryRequest.getAIName();
        String isOnline = aiModelQueryRequest.getIsOnline();
        String aiDescription = aiModelQueryRequest.getAIDescription();
        String sortField = aiModelQueryRequest.getSortField();
        String sortOrder = aiModelQueryRequest.getSortOrder();
        QueryWrapper<Aimodel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(isOnline), "isOnline", isOnline);
        queryWrapper.like(StringUtils.isNotBlank(aiDescription), "aiDescription", aiDescription);
        queryWrapper.like(StringUtils.isNotBlank(aiName), "aiName", aiName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




