package com.fly.springbootinit.scheduleTask;

import com.fly.springbootinit.mapper.UserMapper;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 每日定时更新每个人相应次数
 *
 * @author fly
 */
@Component
@Slf4j
public class ResetLeftCount {
    @Resource
    private UserMapper userMapper;


    @Scheduled( cron = "0 0 0 * * ?" ) // 每天0时触发任务
    public void resetLeftCount() {
        boolean b = userMapper.updateAllUserLeftCountTo100();
        if (!b) {
            log.error("定时更新剩余次数错误");
        }
    }

}
