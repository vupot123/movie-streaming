package com.example.movie_streaming.streamService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Đảm bảo tất cả broker xác nhận
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Số lần thử lại khi gửi thất bại
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Kích thước batch
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Độ trễ tối đa trước khi gửi batch
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip"); // Nén dữ liệu
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 1048576); // Kích thước tối đa của request (1MB)
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // Thời gian chờ giữa các lần retry
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "file-uploaded-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Bắt đầu từ offset sớm nhất
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Tắt auto commit để kiểm soát thủ công
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // Số lượng record tối đa trong mỗi poll
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 15000); // Thời gian timeout session
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000); // Khoảng thời gian heartbeat
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Cấu hình factory cho container listener Kafka.
     * @return ConcurrentKafkaListenerContainerFactory với concurrency và error handling.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // Số lượng thread xử lý song song
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // Xác nhận thủ công
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3L))); // Retry sau 1s, tối đa 3 lần
        return factory;
    }
}