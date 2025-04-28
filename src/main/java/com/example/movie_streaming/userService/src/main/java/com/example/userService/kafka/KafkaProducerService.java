package com.example.userService.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    public void sendMessage(String topic, KafkaMessage message) {
        kafkaTemplate.send(topic, message);
    }
}

