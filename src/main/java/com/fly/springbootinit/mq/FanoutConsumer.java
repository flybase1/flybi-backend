package com.fly.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class FanoutConsumer {
  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();

    // 创建频道
    Channel channelA = connection.createChannel();
    Channel channelB = connection.createChannel();

    // 创建交换机
    channelA.exchangeDeclare(EXCHANGE_NAME, "fanout");
    // 随机分配队列名称channel.queueDeclare().getQueue()
    String queueNameA = "A工作队列" ;
    // 创建队列
    channelA.queueDeclare(queueNameA,true,false,false,null);
    channelA.queueBind(queueNameA, EXCHANGE_NAME, "");

    // 创建交换机
    channelB.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueNameB = "B工作队列" ;
    // 创建队列
    channelB.queueDeclare(queueNameB,true,false,false,null);
    channelB.queueBind(queueNameB, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallbackA = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" A Received '" + message + "'");
    };

    DeliverCallback deliverCallbackB = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" B Received '" + message + "'");
    };

    channelA.basicConsume(queueNameA, true, deliverCallbackA, consumerTag -> { });
    channelB.basicConsume(queueNameB, true, deliverCallbackB, consumerTag -> { });

  }
}