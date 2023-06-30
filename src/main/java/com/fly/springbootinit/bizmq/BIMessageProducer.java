package com.fly.springbootinit.bizmq;

import com.fly.springbootinit.bizmq.Constant.BIMQConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 生产者
 */
@Component
public class BIMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发消息
     * @param message
     */
    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(BIMQConstant.BI_EXCHANGE_NAME,BIMQConstant.BI_ROUTING_KEY,message);
    }
}
