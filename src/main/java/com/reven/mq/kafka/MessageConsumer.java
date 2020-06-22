package com.reven.mq.kafka;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.reven.bo.Order;

/**
 * 消息消费者
 */
@Component
public class MessageConsumer {

    private static final String TOPIC = "testTopic_repeat_003";

    private static final String KAFKA_SUCCESS_CONSUMER_MSG = "kafka:success:consumer:msg:";

    private static final String KAFKA_FAIL_CONSUMER_MSG = "kafka:fail:consumer:msg:";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = TOPIC, groupId = "test-group2", concurrency = "1", containerFactory = "customKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Consumer<String, String> consumer) throws Exception {
        LOGGER.info("消息处理开始，{}", record);
        // 解析消息
        Order order = parseMsg(record);
        // 解析失败或者为空时，关键字段为空
        if (order == null || order.getOrderId() == null) {
            LOGGER.error("order对象解析失败或为空，关键字段orderId为空,order={}", order);
            consumer.commitSync();
            return;
        }

        // 消息的唯一标识——用于去重判断
        String msgId = TOPIC+"_1_"+order.getOrderId();
        try {
            // 去重判断
            Boolean hasKey = redisTemplate.hasKey(KAFKA_SUCCESS_CONSUMER_MSG + msgId);
            if (!hasKey) {
                // 消息处理逻辑
                handleMsg(record, order);
                // 处理成功，记录消息已经消费
                redisTemplate.opsForValue().set(KAFKA_SUCCESS_CONSUMER_MSG + msgId, "", 14, TimeUnit.DAYS);
            } else {
                LOGGER.info("重复消息,唯一标识={}，忽略处理", msgId);
            }
            // 同步提交
            consumer.commitSync();
            LOGGER.info("消费成功topic={},msgId={}", record.topic(), msgId);
        } catch (Exception e) {
            LOGGER.error("消费失败，错误信息{},record=", e.getMessage(), record, e);
            errorHandle(record, consumer, msgId, e);
        }
    }

    private void errorHandle(ConsumerRecord<String, String> record, Consumer<String, String> consumer, String msgId,
            Exception e) throws Exception {
        redisTemplate.opsForValue().increment(KAFKA_FAIL_CONSUMER_MSG + msgId);
        Integer failTimes = (Integer) redisTemplate.opsForValue().get(KAFKA_FAIL_CONSUMER_MSG + msgId);
        if (failTimes >= 5) {
            // 忽略消息，提交位移
            LOGGER.error("消费连续错误5次，跳过该消息：{}", record);
            failHandleMsgNotice(record, e);
            consumer.commitSync();
        } else {
            // 将位移信息指定为当前位移，为了sleep后再次消费
            Set<TopicPartition> topicPartitions = consumer.assignment();
            TopicPartition currentTopicPartition = null;
            for (TopicPartition topicPartition : topicPartitions) {
                if (topicPartition.partition() == record.partition() && topicPartition.topic().equals(record.topic())) {
                    currentTopicPartition = topicPartition;
                }
            }
            consumer.seek(currentTopicPartition, record.offset());

            try {
                // 暂停消费消费3分钟，注意避免引起Rebalance
                Thread.sleep(18000);
            } catch (InterruptedException e1) {
            }
        }
    }

    private void handleMsg(ConsumerRecord<String, String> record, Order order) {
        // 模拟消息中orderId=o_4,处理发生错误
        if ("o_4".equals(order.getOrderId())) {
            int a = (1 / 0);
            LOGGER.info(a + "");
        }
    }

    private Order parseMsg(ConsumerRecord<String, String> record) {
        try {
            String msg = record.value();
            Order order = JSONObject.parseObject(msg, Order.class);
            return order;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            // 解析失败时，跳过消息，并邮件通知人工确认。
            failHandleMsgNotice(record, e);
            throw e;
        }
    }

    private void failHandleMsgNotice(ConsumerRecord<String, String> record, Exception e) {
        String subject = "Kafka消息队列，消费失败提醒";
        String emailContent = record.toString() + getExceptionDetail(e);
        String mailTo = "";
        LOGGER.info("subject={},mailTo={},emailContent={}", subject, mailTo, emailContent);
    }

    public static String getExceptionDetail(Exception e) {
        StringBuffer bf = new StringBuffer();
        bf.append("异常信息：");
        bf.append(e.getMessage());
        bf.append("<br/>");// 页面换行
        StackTraceElement[] stackTraces = e.getStackTrace();
        int i = 0;
        for (StackTraceElement trace : stackTraces) {
            i++;
            bf.append(trace.toString()).append("<br/>");
            if (i > 100) {// 最多打印100行就可以了
                break;
            }
        }
        return bf.toString();
    }
}