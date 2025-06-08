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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String DLQ_TOPIC = "file-uploaded-topic-dlq"; // Sử dụng DLQ duy nhất

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SingleMovieStreamRepository singleMovieStreamRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate,
                                SingleMovieStreamRepository singleMovieStreamRepository,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "file-uploaded-topic", groupId = "file-uploaded-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        String messageJson = consumerRecord.value();
        try {
            logger.info("Received message from Kafka: {}", messageJson);

            // Parse JSON thành KafkaMessage
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            if (payload == null) {
                throw new IllegalArgumentException("Payload không được null");
            }

            String entityType = message.getEntityType();
            String action = message.getAction();

            if ("file-upload".equals(entityType)) {
                switch (action) {
                    case "UPLOAD":
                        handleUpload(payload);
                        break;
                    case "DELETE":
                        handleDelete(payload);
                        break;
                    case "GET":
                    case "GET_ALL":
                        logger.info("Processed {} action for payload: {}", message.getAction(), payload);
                        break;
                    default:
                        logger.warn("Unsupported action: {}", message.getAction());
                        throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
                }
            } else {
                logger.warn("Unsupported entity type: {}", entityType);
                throw new IllegalArgumentException("Loại thực thể không hợp lệ: " + entityType);
            }

            // Xác nhận message đã xử lý thành công
            acknowledgment.acknowledge();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error processing message: {}. Error: {}", messageJson, e.getMessage());
            sendToDlq(messageJson);
        } catch (Exception e) {
            logger.error("Error processing message: {}. Error: {}", messageJson, e.getMessage(), e);
            sendToDlq(messageJson);
        }
    }

    private void handleUpload(Map<String, Object> payload) {
        try {
            String fileName = (String) payload.get("fileName");
            String fileUrl = (String) payload.get("fileUrl");

            if (fileName == null || fileUrl == null) {
                throw new IllegalArgumentException("fileName và fileUrl không được null");
            }

            logger.info("Processing UPLOAD for fileName: {}", fileName);

            // Kiểm tra trùng fileName (đảm bảo nhất quán với GCS)
            if (singleMovieStreamRepository.existsByFileName(fileName)) {
                logger.warn("FileName {} already exists in database.", fileName);
                throw new IllegalArgumentException("File name already exists: " + fileName);
            }

            // Tạo và lưu SingleMovieStream với tên file khớp với GCS
            SingleMovieStream stream = new SingleMovieStream();
            stream.setFileName(fileName); // Sử dụng fileName từ payload (đã được makeUnique ở producer)
            stream.setFileUrl(fileUrl);   // Sử dụng fileUrl từ payload (đã được tạo ở producer)
            singleMovieStreamRepository.save(stream);
            logger.info("Created SingleMovieStream from Kafka: {}", stream);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error handling UPLOAD: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error handling UPLOAD: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during UPLOAD: " + e.getMessage(), e);
        }
    }

    private void handleDelete(Map<String, Object> payload) {
        try {
            String fileName = (String) payload.get("fileName");

            if (fileName == null) {
                throw new IllegalArgumentException("fileName không được null");
            }

            logger.info("Processing DELETE for fileName: {}", fileName);

            Optional<SingleMovieStream> streamOpt = singleMovieStreamRepository.findByFileName(fileName);
            if (streamOpt.isPresent()) {
                singleMovieStreamRepository.delete(streamOpt.get());
                logger.info("Deleted SingleMovieStream for fileName: {}", fileName);
            } else {
                logger.warn("No SingleMovieStream found for fileName: {}", fileName);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error handling DELETE: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error handling DELETE: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during DELETE: " + e.getMessage(), e);
        }
    }

    private void sendToDlq(String messageJson) {
        try {
            // Gửi message vào DLQ đồng bộ
            kafkaTemplate.send(DLQ_TOPIC, messageJson).get(); // Chặn thread để chờ gửi xong
            logger.info("Successfully sent to DLQ: {}", messageJson);
        } catch (InterruptedException e) {
            logger.error("Interrupted while sending to DLQ: {}. Error: {}", messageJson, e.getMessage());
            Thread.currentThread().interrupt(); // Khôi phục trạng thái gián đoạn
        } catch (ExecutionException e) {
            logger.error("Execution error while sending to DLQ: {}. Error: {}", messageJson, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while sending to DLQ: {}. Error: {}", messageJson, e.getMessage());
        }
    }
}