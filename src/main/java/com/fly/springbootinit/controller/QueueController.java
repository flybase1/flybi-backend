package com.fly.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.config.ThreadPoolExecutorConfig;
import com.fly.springbootinit.exception.BusinessException;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping( "/queue" )
@Profile({"dev","local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    @GetMapping( "/add" )
    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            System.out.println("任务执行中" + name+",执行人"+Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前服务器请求紧张，请稍后再试");
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get(){
        Map<String,Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度",size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数",taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("完成任务总数",completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数",activeCount);
        int corePoolSize = threadPoolExecutor.getCorePoolSize();
        map.put("核心线程数",corePoolSize);
        return JSONUtil.toJsonStr(map);
    }

}
