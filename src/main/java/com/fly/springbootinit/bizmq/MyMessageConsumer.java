package com.fly.springbootinit.bizmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MyMessageConsumer {
    //指定程序监听的消息队列确认机制
    @SneakyThrows
    @RabbitListener( queues = {"code_queue"}, ackMode = "MANUAL" )
    public void receiveMessage(String message, Channel channel, @Header( AmqpHeaders.DELIVERY_TAG ) long deliverTag) {
        log.info("receive message = {}", message);
        channel.basicAck(deliverTag, false);
    }
}
