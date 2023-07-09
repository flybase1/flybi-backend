package com.fly.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生产者
 */
//@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

//    public void sendMessage(String exchange,String routingKey,String message){
//        rabbitTemplate.convertAndSend(exchange,routingKey,message);
//    }
}
