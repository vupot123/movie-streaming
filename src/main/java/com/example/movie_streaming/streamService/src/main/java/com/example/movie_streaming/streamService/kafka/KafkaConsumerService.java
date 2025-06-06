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

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate,
                                SingleMovieStreamRepository singleMovieStreamRepository,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "file-upload-topic", groupId = "file-upload-group",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        try {
            logger.info("📥 Nhận từ Kafka: {}", messageJson);

            // Parse JSON thành KafkaMessage
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();
            if (payload == null) {
                throw new IllegalArgumentException("Payload không được null");
            }

            if (message.getEntityType().equals("file-upload")) {
                switch (message.getAction()) {
                    case "UPLOAD":
                        handleUpload(payload);
                        break;
                    case "DELETE":
                        handleDelete(payload);
                        break;
                    default:
                        logger.warn("Hành động không xác định: {}", message.getAction());
                        throw new IllegalArgumentException("Hành động không hợp lệ: " + message.getAction());
                }
            } else {
                logger.warn("Loại thực thể không xác định: {}", message.getEntityType());
                throw new IllegalArgumentException("Loại thực thể không hợp lệ: " + message.getEntityType());
            }

            // Xác nhận sau khi xử lý thành công
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý thông điệp: {}. Lỗi: {}", messageJson, e.getMessage(), e);
            // Gửi thông điệp vào DLQ (Dead Letter Queue) nếu có lỗi
            kafkaTemplate.send(DLQ_TOPIC, messageJson);
        }
    }

    private void handleUpload(Map<String, Object> payload) {
        Long fileId = payload.get("fileId") != null ? Long.valueOf(payload.get("fileId").toString()) : null;
        String fileName = (String) payload.get("fileName");
        String fileUrl = (String) payload.get("fileUrl");
        String contentType = (String) payload.get("contentType");

        if (fileId == null || fileUrl == null) {
            throw new IllegalArgumentException("fileId và fileUrl không được null");
        }

        // Kiểm tra trùng fileUrl
        String uniqueFileUrl = makeUniqueFileUrl(fileUrl);

        // Tạo và lưu SingleMovieStream
        SingleMovieStream stream = new SingleMovieStream(fileId, fileName, uniqueFileUrl);
        singleMovieStreamRepository.save(stream);
        logger.info("Đã tạo SingleMovieStream từ Kafka: {}", stream);
    }

    private void handleDelete(Map<String, Object> payload) {
        Long fileId = payload.get("fileId") != null ? Long.valueOf(payload.get("fileId").toString()) : null;
        if (fileId == null) {
            throw new IllegalArgumentException("fileId không được null");
        }

        singleMovieStreamRepository.deleteByFileId(fileId);
        logger.info("Đã xóa SingleMovieStream cho fileId: {}", fileId);
    }

    private String makeUniqueFileUrl(String fileUrl) {
        if (fileUrl == null) {
            throw new IllegalArgumentException("fileUrl không được null");
        }

        String uniqueFileUrl = fileUrl;
        int suffix = 1;

        // Kiểm tra trùng fileUrl trong cơ sở dữ liệu
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
