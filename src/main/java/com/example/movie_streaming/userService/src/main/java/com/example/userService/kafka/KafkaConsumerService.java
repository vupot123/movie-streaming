package com.example.userService.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "user-registration", groupId = "group_id")
    public void listen(String message) {
        log.info("Received message from Kafka: {}", message);
    }
}
