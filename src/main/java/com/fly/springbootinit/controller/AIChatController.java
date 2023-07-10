package com.fly.springbootinit.controller;

import com.fly.springbootinit.common.BaseResponse;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.common.ResultUtils;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.model.entity.Aichat;
import com.fly.springbootinit.service.AichatService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping( "/AIChart" )
@RestController
public class AIChatController {
    @Resource
    private AichatService aichatService;

    @PostMapping( "/add" )
    public BaseResponse<Boolean> reply(String message, HttpServletRequest request) {
        Boolean addChat = aichatService.doAddChat(message, request);
        if (!addChat) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }


    @GetMapping( "/list" )
    public BaseResponse<List<Aichat>> listAllChat(HttpServletRequest request) {
        List<Aichat> aichats = aichatService.listUserChat(request);
        return ResultUtils.success(aichats);
    }
}
