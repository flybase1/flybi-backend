package com.fly.springbootinit.bizmq;

import com.fly.springbootinit.bizmq.Constant.BIMQConstant;
import com.fly.springbootinit.common.ErrorCode;
import com.fly.springbootinit.exception.BusinessException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * BI消息队列
 */
public class BIInitLocalMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");

            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            // 正常队列
            String channelExchange = BIMQConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(channelExchange, "direct");
            String queueName = BIMQConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName, false, false, false, null);
            String routingKey = BIMQConstant.BI_ROUTING_KEY;
            channel.queueBind(queueName, channelExchange, routingKey);

            // 死信队列
            String queueDLXName = BIMQConstant.BI_DLX_DIRECT_QUEUE_NAME;
            String channelDLXExchange = BIMQConstant.BI_DLX_DIRECT_EXCHANGE_NAME;
            String routingDLXKey = BIMQConstant.BI_DLX_DIRECT_ROUTING_KEY_NAME;
            channel.queueDeclare(queueDLXName, false, false, false, null);
            channel.exchangeDeclare(channelDLXExchange, "direct");
            channel.queueBind(queueName, channelExchange, routingDLXKey);


            /*            channel.queueBind(queueName, channelExchange, BIMQConstant.BI_ROUTING_KEY);*/
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "服务器系统故障");
        }

    }
}
