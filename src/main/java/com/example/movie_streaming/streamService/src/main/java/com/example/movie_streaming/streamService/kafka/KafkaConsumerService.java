package com.example.movie_streaming.streamService.kafka;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SingleMovieStreamRepository singleMovieStreamRepository;
    private final ObjectMapper objectMapper;
    private static final String DLQ_TOPIC = "file-upload-topic-dlq";

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate, SingleMovieStreamRepository singleMovieStreamRepository, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "file-upload-topic", groupId = "file-upload-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        try {
            logger.info("📥 Received from Kafka: {}", messageJson);

            // Parse JSON thành KafkaMessage
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            if (message.getEntityType().equals("file-upload")) {
                switch (message.getAction()) {
                    case "UPLOAD":
                        handleUpload(payload);
                        break;
                    case "DELETE":
                        handleDelete(payload);
                        break;
                    default:
                        logger.warn("Unknown action: {}", message.getAction());
                }
            } else {
                logger.warn("Unknown entity type: {}", message.getEntityType());
            }

            // Acknowledge sau khi xử lý thành công
            acknowledgment.acknowledge();  // Xác nhận
        } catch (Exception e) {
            logger.error("❌ Error processing message: {}. Error: {}", messageJson, e.getMessage(), e);
            // Gửi tin nhắn vào DLQ (Dead Letter Queue) nếu có lỗi
            kafkaTemplate.send(DLQ_TOPIC, messageJson);
        }
    }

    private void handleUpload(Map<String, Object> payload) {
        Long movieId = Long.valueOf(payload.get("movieId").toString());
        String fileName = (String) payload.get("fileName");
        String fileUrl = (String) payload.get("fileUrl");
        String contentType = (String) payload.get("contentType");

        // Kiểm tra trùng fileUrl
        String uniqueFileUrl = makeUniqueFileUrl(fileUrl);

        // Tạo và lưu SingleMovieStream
        SingleMovieStream stream = new SingleMovieStream(movieId, fileName, uniqueFileUrl);
        singleMovieStreamRepository.save(stream);
        logger.info("Created SingleMovieStream from Kafka: {}", stream);
    }

    private void handleDelete(Map<String, Object> payload) {
        Long movieId = Long.valueOf(payload.get("movieId").toString());
        singleMovieStreamRepository.deleteByMovieId(movieId);
        logger.info("Deleted SingleMovieStream for movieId: {}", movieId);
    }

    private String makeUniqueFileUrl(String fileUrl) {
        String uniqueFileUrl = fileUrl;
        int suffix = 1;

        // Kiểm tra trùng fileUrl trong database
        while (singleMovieStreamRepository.existsByFileUrl(uniqueFileUrl)) {
            String[] parts = fileUrl.split("\\.(?=[^.]+$)");
            if (parts.length == 2) {
                uniqueFileUrl = parts[0] + "_" + suffix + "." + parts[1];
            } else {
                uniqueFileUrl = fileUrl + "_" + suffix;
            }
            suffix++;
        }
        return uniqueFileUrl;
    }
}
