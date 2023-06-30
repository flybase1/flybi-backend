package com.fly.springbootinit.bizmq.Constant;

/**
 *
 */
public interface BIMQConstant {
    String BI_QUEUE_NAME = "bi_queue";
    String BI_EXCHANGE_NAME = "bi_exchange";
    String BI_ROUTING_KEY = "bi_routingKey";

    /**
     * 死信交换机，死性队列
     */
    String BI_DLX_DIRECT_EXCHANGE_NAME = "bi_dlx_direct_exchange";
    String BI_DLX_DIRECT_QUEUE_NAME = "bi_dlx_direct_queue";
    String BI_DLX_DIRECT_ROUTING_KEY_NAME = "bi_dlx_direct_routingKey";
}
