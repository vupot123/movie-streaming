package com.example.movie_streaming.movieService.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
<<<<<<< HEAD

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public void sendMessage(String topic, KafkaMessage message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("Kafka message sent successfully to topic {}: {}", topic, message);
        } catch (Exception e) {
            log.error("Failed to send Kafka message to topic {}: {}", topic, e.getMessage());
        }
    }
}

=======
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info(" Sent to Kafka topic {}: {} with offset: {}",
                        topic, message, result.getRecordMetadata().offset());
            } else {
                logger.error(" Failed to send to Kafka topic {}: {}, error: {}",
                        topic, message, ex.getMessage());
            }
        });
    }
}
>>>>>>> dev
