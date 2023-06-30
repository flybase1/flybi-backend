package com.fly.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.AMQConnection;

/**
 * 创建一次测试
 */
public class MqInitMain {
    public static void main(String[] args) {

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");

            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String channelExchange = "code_exchange";
            channel.exchangeDeclare(channelExchange, "direct");

            String queueName = "code_queue";
            channel.queueDeclare(queueName, false, false, false, null);

            channel.queueBind(queueName, channelExchange, "my_routingKey");
        } catch (Exception e) {

        }

    }
}
