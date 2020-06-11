package com.reven.mq.kafka;

import java.util.HashMap;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ManualProducerConfig {
    @Value("${custom.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${custom.kafka.producer.retries}")
    private String retries;
    @Value("${custom.kafka.producer.batch-size}")
    private String batchSize;
    @Value("${custom.kafka.producer.buffer-memory}")
    private String buffeMemory;
    @Value("${custom.kafka.producer.linger.ms}")
    private String lingerMs;

    @Value("${custom.kafka.producer.acks}")
    private String acks;
    @Value("${custom.kafka.producer.compression-type}")
    private String compressionType;
    @Value("${custom.kafka.producer.connections.max.idle.ms}")
    private String connectionsMaxIdleMs;

    @Value("${custom.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${custom.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${custom.kafka.producer.enable.idempotence}")
    private String enableIdempotence;
    
    @Bean("producerKafkaTemplate")
    public KafkaTemplate<String, String> kafkaTemplate() {
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        configs.put(ProducerConfig.RETRIES_CONFIG, retries);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, buffeMemory);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);

        configs.put(ProducerConfig.ACKS_CONFIG, acks);
        configs.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, connectionsMaxIdleMs);
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        // 设置序列化
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<String, String>(
                configs);
        return new KafkaTemplate<String, String>(producerFactory);
    }
}