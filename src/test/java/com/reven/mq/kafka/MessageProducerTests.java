package com.reven.mq.kafka;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.reven.bo.Order;

@SpringBootApplication(scanBasePackages = "com.reven")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(value = "com.reven", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        MessageConsumer.class }))
public class MessageProducerTests {

    @Autowired
    private MessageProducer messageProducer;

    /**   
     * @Title: sendSameMsg   
     */
    @Test
    void sendSameMsg() throws InterruptedException, ExecutionException {
        Order order = new Order();
        String orderId = "o_" + 2;
        order.setOrderId(orderId);
        order.setAmount(new BigDecimal(1000.08));
        order.setCustomerName("顺序消息");
        order.setOrderDate(new Date());
        // 用于消息分配到不同的分区
        String msgKey = "order_group_6";
        messageProducer.sendOrderMsg("testTopic_repeat3",order, msgKey);
        messageProducer.sendOrderMsg("testTopic_repeat3",order, msgKey);
    }
    
    @Test
    void sendOtherParttion() throws InterruptedException, ExecutionException {
        // 用于消息分配到不同的分区
        String msgKey = "order_group_1";
        
        Order order = new Order();
        String orderId = "o_" + 5;
        order.setOrderId(orderId);
        order.setAmount(new BigDecimal(1000.08));
        order.setCustomerName("正常消息");
        order.setOrderDate(new Date());
        messageProducer.sendOrderMsg("testTopic_repeat3",order, msgKey);
        Order order2 = new Order();
        String orderId2 = "o_" + 4;
        order2.setOrderId(orderId2);
        order2.setAmount(new BigDecimal(1000.08));
        order2.setCustomerName("错误消息");
        order2.setOrderDate(new Date());
        messageProducer.sendOrderMsg("testTopic_repeat3",order2, msgKey);
    }
    
    @Test
    void sendError() throws InterruptedException, ExecutionException {
        Order order2 = new Order();
        String orderId2 = "o_" + 4;
        order2.setOrderId(orderId2);
        order2.setAmount(new BigDecimal(1000.01));
        order2.setCustomerName(buildBigString());
        order2.setOrderDate(new Date());
        messageProducer.sendOrderMsg("testTopic_repeat3",order2, "test");
    }
    private String buildBigString() {
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < 1000000; i++) {
            sb.append(i+"我是很长很长的字符串。");
        }
        return sb.toString();
    }

}
