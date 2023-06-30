package com.fly.springbootinit.mq;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        // 连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        // 频道
        Channel channel = connection.createChannel();

        // 交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 队列AA
        String queueName = "AA Queue";
        channel.queueDeclare(queueName, true, false, false, null);
        // 绑定
        channel.queueBind(queueName, EXCHANGE_NAME, "AA");

        // 队列BB
        String queueNameBB = "BB Queue";
        channel.queueDeclare(queueNameBB, true, false, false, null);
        // 绑定
        channel.queueBind(queueNameBB, EXCHANGE_NAME, "BB");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallbackAA = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" AA Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback deliverCallbackBB = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" BB Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 启动监听
        channel.basicConsume(queueName, true, deliverCallbackAA, consumerTag -> {
        });


        // 启动监听
        channel.basicConsume(queueNameBB, true, deliverCallbackBB, consumerTag -> {
        });
    }
}