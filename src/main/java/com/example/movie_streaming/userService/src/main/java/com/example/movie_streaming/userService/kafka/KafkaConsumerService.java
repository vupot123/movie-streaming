package com.example.movie_streaming.userService.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-registration", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(KafkaMessage message) {
        log.info("Consumed message: {}", message);
    }
}
