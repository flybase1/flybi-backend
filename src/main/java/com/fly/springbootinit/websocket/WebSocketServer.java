package com.fly.springbootinit.websocket;

import com.fly.springbootinit.mapper.ChartMapper;
import com.fly.springbootinit.model.entity.Chart;
import com.fly.springbootinit.model.entity.User;
import com.fly.springbootinit.model.enums.ChartStatusEnum;
import com.fly.springbootinit.service.ChartService;
import com.fly.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接步骤
 * 返回单个数据
 */

@ServerEndpoint( value = "/websocket" )
@Component
@Slf4j
public class WebSocketServer {
    private static Logger L = LoggerFactory.getLogger(WebSocketServer.class);
    private Long chartId;
    private ScheduledExecutorService scheduler;
    @Resource
    private ChartService chartService;
    private Session session;
    @Resource
    private ChartMapper chartMapper;
    @Resource
    private UserService userService;


    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        Map<String, Object> map = selectLeatestChart();
        if (map == null) {
            // 如果图表信息为空，则关闭连接
            try {
                session.close();
                log.info("连接关闭：图表信息为空");
            } catch (IOException e) {
                log.error("关闭连接时发生异常：", e);
            }
        } else {
            // 如果图表信息不为空，则开启连接并发送消息
            try {
                // 发送消息
                session.getBasicRemote().sendText("消息收到");
                log.info("消息发送成功");

                // 开启定时任务，定时发送消息
                startSendingMessages(session);
            } catch (IOException e) {
                log.error("发送消息时发生异常：", e);
            }
        }
        log.info("建立成功=====>" + chartId);
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose() {

        log.info("关闭连接");
    }

    /**
     * 发送消息
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText("消息收到");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Map<String, Object> selectLeatestChart() {
        Map<String, Object> map = chartMapper.selectLatestChart();
        return map;
    }

    /**
     * 发送定时消息
     *
     * @param session
     */
    private void startSendingMessages(Session session) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger counter = new AtomicInteger(0);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int messageNumber = counter.incrementAndGet();
                String message = "定时消息" + messageNumber;
                session.getBasicRemote().sendText(message);
                System.out.println("发送消息: " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // 每隔5秒发送一次消息


    }

    private void stopSendingMessages() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

}
