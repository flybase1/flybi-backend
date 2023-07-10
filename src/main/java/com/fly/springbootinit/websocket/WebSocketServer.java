package com.fly.springbootinit.websocket;

import com.fly.springbootinit.api.YuApi;
import com.fly.springbootinit.constant.CommonConstant;
import com.fly.springbootinit.model.entity.Aichat;
import com.fly.springbootinit.service.AichatService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接步骤
 * 返回单个数据
 */

//@ServerEndpoint( value = "/websocket/{user_id}" )
//@Component
@Slf4j
public class WebSocketServer {
    private static Logger L = LoggerFactory.getLogger(WebSocketServer.class);
    private String userId;
    private ScheduledExecutorService scheduler;
    @Resource
    private AichatService aichatService;
    @Resource
    private YuApi yuApi;


    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, @PathParam( "user_id" ) String userId) {
        this.userId = userId;
        log.info("ok");
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
            session.getBasicRemote().sendText("ok===>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
