/*
 * Copyright (c) 2000-2019 All Rights Reserved.
 */
package com.reven.mq.kafka;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.reven.bo.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息生产者
 */
@Component
@Slf4j
public class MessageProducer {
    @Autowired
    private KafkaTemplate<String, String> producerKafkaTemplate;

    public void sendOrderMsg(String topic, Order order, String msgKey) throws InterruptedException, ExecutionException {
        // 同步发送需要等待kafka服务器的响应吞吐相对较低
        String jsonMsg = JSON.toJSONString(order);
        SendResult<String, String> sendResult = producerKafkaTemplate.send(topic, msgKey, jsonMsg).get();
        log.info(sendResult.toString());
    }
    
    public void sendOrderMsg(String topic, String msg, String msgKey) throws InterruptedException, ExecutionException {
        // 同步发送需要等待kafka服务器的响应吞吐相对较低
        SendResult<String, String> sendResult = producerKafkaTemplate.send(topic, msgKey, msg).get();
        log.info(sendResult.toString());
    }

}
