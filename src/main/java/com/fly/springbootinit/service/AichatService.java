package com.fly.springbootinit.service;

import com.fly.springbootinit.model.entity.Aichat;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author admin
 * @description 针对表【aichat(AI聊天)】的数据库操作Service
 * @createDate 2023-07-09 18:57:11
 */
public interface AichatService extends IService<Aichat> {

    /**
     * 添加对话
     * @param message
     * @param request
     * @return
     */
    Boolean doAddChat(String message, HttpServletRequest request);


    /**
     * 展示所有的数据
     * @param request
     * @return
     */
    List<Aichat> listUserChat(HttpServletRequest request);

}
