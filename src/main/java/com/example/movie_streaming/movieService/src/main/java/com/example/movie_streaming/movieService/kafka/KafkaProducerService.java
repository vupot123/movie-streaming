package com.example.movie_streaming.movieService.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("Kafka message sent successfully to topic {}: {}", topic, message);
        } catch (Exception e) {
            log.error("Failed to send Kafka message to topic {}: {}", topic, e.getMessage());
        }
    }
}
