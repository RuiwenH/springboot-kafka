package com.reven.mq.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class ManualConsumerConfig {
    @Value("${custom.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${custom.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value("${custom.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${custom.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;
    @Value("${custom.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${custom.kafka.consumer.max-poll-records}")
    private String maxPollRecords;
    
    @Value("${custom.kafka.consumer.max-poll-interval-ms}")
    private String MAX_POLL_INTERVAL_MS_CONFIG;
    @Value("${custom.kafka.consumer.session-timeout-ms}")
    private String SESSION_TIMEOUT_MS_CONFIG;
    @Value("${custom.kafka.consumer.heartbeat-interval-ms}")
    private String HEARTBEAT_INTERVAL_MS_CONFIG;


    @Bean
    public KafkaListenerContainerFactory<?> manualKafkaListenerContainerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        // 手动提交
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS_CONFIG);
        /**
         * 一些说明 1、Consumer未在规定时间内发送心跳请求。规定时间：session.timeout.ms，默认10s；
         * 心跳发送请求时间间隔heartbeat.interval.ms，建议设置小一些session.timeout.ms的1/3。 2、限定了 Consumer
         * 端应用程序两次调用 poll
         * 方法的最大时间间隔。max.poll.interval.ms默认值5分钟，如果未能在该时间内poll第二次消息，则认为Consumer“离线”
         *
         * 不必要发生的情况 1、消费一批消息的时间超过max.poll.interval.ms
         * 2、合理的设置session.timeout.ms>=3*heartbeat.interval.ms ,推荐值 3、避免Consumer端频繁Full
         * GC的长时间停顿导致Rebalance发生。
         */
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS_CONFIG);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS_CONFIG);

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(configProps));
        // ack模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

}
