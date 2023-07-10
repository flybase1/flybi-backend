package com.fly.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.springbootinit.api.YuApi;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.exception.BusinessException;
import com.fly.springbootinit.model.entity.Aichat;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.AichatService;
import com.fly.springbootinit.mapper.AichatMapper;
import com.fly.springbootinit.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【aichat(AI聊天)】的数据库操作Service实现
 * @createDate 2023-07-09 18:57:11
 */
@Service
public class AichatServiceImpl extends ServiceImpl<AichatMapper, Aichat>
        implements AichatService {

    @Resource
    private UserService userService;

    @Resource
    private YuApi yuApi;

    @Override
    public Boolean doAddChat(String message, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        String userAvatar = loginUser.getUserAvatar();
        Aichat aichat = new Aichat();
        aichat.setAIAvatar("https://picsum.photos/200/300");
        aichat.setUserId(loginUserId);
        aichat.setUserMessage(message);
        aichat.setUserAvatar(userAvatar);
        aichat.setUserName(loginUser.getUserAccount());
        String replyMessage = yuApi.doChart(CommonConstant.CHAT_MODEL_ID, message);
        if (replyMessage == null) {
            replyMessage = "网络错误";
        }
        boolean b = userService.updateUserChartCount(request);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        aichat.setAIMessage(replyMessage);
        return this.save(aichat);
    }


    @Override
    public List<Aichat> listUserChat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = loginUser.getId();
        QueryWrapper<Aichat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
//        queryWrapper.orderByDesc("updateTime");
        List<Aichat> list = this.list(queryWrapper);
        return list;
    }


}




