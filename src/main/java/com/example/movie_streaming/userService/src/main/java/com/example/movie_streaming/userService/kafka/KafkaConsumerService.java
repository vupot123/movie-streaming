package com.example.movie_streaming.userService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String DLQ_TOPIC = "user-registration-dlq";

    public KafkaConsumerService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "user-registration", groupId = "user-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        log.info(" Received from Kafka topic user-registration, partition={}, offset={}: {}",
                record.partition(), record.offset(), messageJson);

        try {
            // Parse JSON thành KafkaMessage
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            log.debug("Parsed KafkaMessage: {}", message);

            // Xử lý message
            if ("user".equals(message.getEntityType()) && "REGISTER".equals(message.getAction())) {
                handleRegister(message);
            } else {
                log.warn("Unknown message: entityType={}, action={}",
                        message.getEntityType(), message.getAction());
            }

            // Commit offset
            acknowledgment.acknowledge();
            log.debug("Committed offset for partition={}, offset={}", record.partition(), record.offset());
        } catch (Exception e) {
            log.error(" Error processing message: {}. Sending to DLQ: {}", messageJson, DLQ_TOPIC, e);
            kafkaTemplate.send(DLQ_TOPIC, messageJson);
            acknowledgment.acknowledge();
        }
    }

    private void handleRegister(KafkaMessage message) {
        Long userId = message.getEntityId();
        var payload = message.getPayload();
        String username = (String) payload.get("username");
        String email = (String) payload.get("email");

        log.info("Processing REGISTER for user: id={}, username={}, email={}", userId, username, email);

    }
}