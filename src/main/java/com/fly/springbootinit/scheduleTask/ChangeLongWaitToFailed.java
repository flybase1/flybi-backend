package com.fly.springbootinit.scheduleTask;

import com.fly.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 将长时间未完成的解析设置为失败
 */
@Component
@Slf4j
public class ChangeLongWaitToFailed {

    @Resource
    private ChartService chartService;

    @Scheduled( cron = "0 0/10 * * * ?" )
    public void changeStatus() {
        boolean b = chartService.ChangeLongTimeWaitToFailed();
        if (!b) {
            log.error("ChangeLongWaitToFailed failed ===>");
        }
    }
}
