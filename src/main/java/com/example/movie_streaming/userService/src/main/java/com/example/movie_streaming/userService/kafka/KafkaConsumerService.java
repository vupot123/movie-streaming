package com.example.movie_streaming.userService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    @KafkaListener(topics = {"user-registration", "user-events", "favorite-events", "movie-views"},
            groupId = "user-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        String topic = record.topic();
        log.info("Received message from Kafka topic '{}', partition={}, offset={}: {}",
                topic, record.partition(), record.offset(), messageJson);

        try {
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            log.debug("Successfully parsed KafkaMessage: {}", message);

            String entityType = message.getEntityType();
            String action = message.getAction();

            if ("user".equals(entityType)) {
                if ("REGISTER".equals(action)) {
                    handleRegister(message);
                } else if ("UPDATE".equals(action)) {
                    handleUpdate(message);
                } else {
                    log.warn("Unsupported action '{}' for entityType 'user'", action);
                }
            } else if ("favorite".equals(entityType)) {
                if ("ADD".equals(action)) {
                    handleFavoriteAdd(message);
                } else if ("REMOVE".equals(action)) {
                    handleFavoriteRemove(message);
                } else {
                    log.warn("Unsupported action '{}' for entityType 'favorite'", action);
                }
            } else if ("movie-view".equals(entityType)) {
                if ("VIEW".equals(action)) {
                    handleMovieView(message);
                } else {
                    log.warn("Unsupported action '{}' for entityType 'movie-view'", action);
                }
            } else {
                log.warn("Unknown entityType: {}", entityType);
            }

            acknowledgment.acknowledge();
            log.debug("Successfully committed offset for partition={}, offset={}", record.partition(), record.offset());
        } catch (Exception e) {
            log.error("Error processing message from topic '{}': {}. Attempting to send to DLQ: {}",
                    topic, messageJson, DLQ_TOPIC, e);
            try {
                kafkaTemplate.send(DLQ_TOPIC, messageJson).get(); // Có thể ném InterruptedException hoặc ExecutionException
                log.info("Successfully sent message to DLQ topic: {}", DLQ_TOPIC);
            } catch (InterruptedException ie) {
                log.error("Interrupted while sending to DLQ for topic '{}': {}", topic, ie.getMessage());
                Thread.currentThread().interrupt(); // Khôi phục trạng thái interrupted
            } catch (ExecutionException ee) {
                log.error("Execution failed while sending to DLQ for topic '{}': {}", topic, ee.getCause().getMessage());
            } catch (Exception e2) {
                log.error("Unexpected error while sending to DLQ for topic '{}': {}", topic, e2.getMessage());
            } finally {
                acknowledgment.acknowledge(); // Luôn commit offset để tránh lặp lại
            }
        }
    }

    private void handleRegister(KafkaMessage message) {
        Long userId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        String username = (String) payload.get("username");
        String email = (String) payload.get("email");

        if (username == null || email == null) {
            log.warn("Missing required fields in REGISTER payload for userId: {}", userId);
            return;
        }

        log.info("Processing REGISTER for user: id={}, username={}, email={}", userId, username, email);
        // Thêm logic xử lý nếu cần
    }

    private void handleUpdate(KafkaMessage message) {
        Long userId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        String username = (String) payload.get("username");
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");

        if (username == null || email == null || name == null) {
            log.warn("Missing required fields in UPDATE payload for userId: {}", userId);
            return;
        }

        log.info("Processing UPDATE for user: id={}, username={}, email={}, name={}", userId, username, email, name);
        // Thêm logic xử lý nếu cần
    }

    private void handleFavoriteAdd(KafkaMessage message) {
        Long userId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        Long movieId = (Long) payload.get("movieId");

        if (movieId == null) {
            log.warn("Missing movieId in ADD payload for userId: {}", userId);
            return;
        }

        log.info("Processing ADD for favorite: userId={}, movieId={}", userId, movieId);
        // Thêm logic xử lý nếu cần
    }

    private void handleFavoriteRemove(KafkaMessage message) {
        Long userId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        Long movieId = (Long) payload.get("movieId");

        if (movieId == null) {
            log.warn("Missing movieId in REMOVE payload for userId: {}", userId);
            return;
        }

        log.info("Processing REMOVE for favorite: userId={}, movieId={}", userId, movieId);
        // Thêm logic xử lý nếu cần
    }

    private void handleMovieView(KafkaMessage message) {
        Long userId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        Long movieId = (Long) payload.get("movieId");

        if (movieId == null) {
            log.warn("Missing movieId in VIEW payload for userId: {}", userId);
            return;
        }

        log.info("Processing VIEW for movie-view: userId={}, movieId={}", userId, movieId);
        // Thêm logic xử lý nếu cần
    }
}