package com.fly.springbootinit.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fly.springbootinit.common.BaseResponse;
import com.fly.springbootinit.common.DeleteRequest;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.common.ResultUtils;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.model.dto.aimodel.AiModelAddRequest;
import com.fly.springbootinit.model.dto.aimodel.AiModelQueryRequest;
import com.fly.springbootinit.model.dto.aimodel.AiModelUpdateRequest;
import com.fly.springbootinit.model.dto.aimodel.DeleteAiModelRequest;
import com.fly.springbootinit.model.entity.Aimodel;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.AimodelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 创建ai模型应用
 */
@RestController
@RequestMapping( "/ai" )
@Slf4j
public class AIModelController {

    @Resource
    private AimodelService aimodelService;

    @PostMapping( "/add" )
    public BaseResponse<Aimodel> addAiModel(@RequestBody AiModelAddRequest aiModelAddRequest) {
        Aimodel aimodel = aimodelService.AddAiModel(aiModelAddRequest);
        return ResultUtils.success(aimodel);
    }

    @GetMapping( "/getAi" )
    public BaseResponse<Aimodel> getAiModelById(Long AIId) {
        Aimodel aimodel = aimodelService.getById(AIId);
        if (aimodel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(aimodel);
    }

    @PostMapping( "/list" )
    public BaseResponse<Page<Aimodel>> listAiModel(@RequestBody AiModelQueryRequest aiModelQueryRequest) {
        long current = aiModelQueryRequest.getCurrent();
        long size = aiModelQueryRequest.getPageSize();
        Page<Aimodel> aimodelPage = aimodelService.page(new Page<>(current, size),
                aimodelService.getQueryWrapper(aiModelQueryRequest));
        return ResultUtils.success(aimodelPage);
    }

    /**
     * 显示所有的ai模型，取出已经上线的
     *
     * @param aName
     * @return
     */
    @GetMapping( "/listAll" )
    public BaseResponse<List<Aimodel>> listAiModel(String aName) {
        QueryWrapper<Aimodel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isOnline", "OnLine");
        if (aName == null) {
            List<Aimodel> list = aimodelService.list(queryWrapper);
            return ResultUtils.success(list);
        }

        queryWrapper.like("aiName", aName);
        List<Aimodel> list = aimodelService.list(queryWrapper);
        return ResultUtils.success(list);
    }

    @PostMapping( "/deleteAI" )
    public BaseResponse<Boolean> deleteAiModel(@RequestBody DeleteAiModelRequest deleteAIModelRequest) {
        if (deleteAIModelRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long AIId = deleteAIModelRequest.getId();
        if (AIId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = aimodelService.removeById(AIId);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(b);
    }

    @PostMapping( "/update" )
    public BaseResponse<Aimodel> updateAiModel(@RequestBody AiModelUpdateRequest aiModelUpdateRequest) {
        if (aiModelUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Aimodel aimodel = new Aimodel();
        BeanUtils.copyProperties(aiModelUpdateRequest, aimodel);
        boolean b = aimodelService.updateById(aimodel);
        if (!b) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(aimodel);
    }



}
