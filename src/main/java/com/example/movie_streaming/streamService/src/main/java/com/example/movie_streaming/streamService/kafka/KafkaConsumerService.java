package com.example.movie_streaming.streamService.kafka;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String DLQ_TOPIC = "file-uploaded-topic-dlq"; // Sử dụng DLQ duy nhất

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SingleMovieStreamRepository singleMovieStreamRepository;
    private final ObjectMapper objectMapper;
    private final Storage storage; // Thêm dependency để xóa file trên GCS
    @Value("${spring.cloud.gcp.storage.bucket}") // Thêm bucketName
    private String bucketName;

    @Autowired
    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate,
                                SingleMovieStreamRepository singleMovieStreamRepository,
                                ObjectMapper objectMapper,
                                Storage storage) {
        this.kafkaTemplate = kafkaTemplate;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
        this.objectMapper = objectMapper;
        this.storage = storage;
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
                        logger.info("Received {} action for payload: {}. Ignoring as direct DB query is used.", message.getAction(), payload);
                        break;
                    default:
                        logger.warn("Unsupported action: {}. Ignoring message: {}", message.getAction(), messageJson);
                        throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
                }
            } else {
                logger.warn("Unsupported entity type: {}. Ignoring message: {}", entityType, messageJson);
                throw new IllegalArgumentException("Loại thực thể không hợp lệ: " + entityType);
            }

            // Xác nhận message đã xử lý thành công
            acknowledgment.acknowledge();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error processing message: {}. Error: {}. Sending to DLQ.", messageJson, e.getMessage());
            sendToDlq(messageJson);
        } catch (Exception e) {
            logger.error("Error processing message: {}. Error: {}. Sending to DLQ.", messageJson, e.getMessage(), e);
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
                logger.warn("FileName {} already exists in database. Skipping upload.", fileName);
                throw new IllegalArgumentException("File name already exists: " + fileName);
            }

            // Tạo và lưu SingleMovieStream với tên file khớp với GCS
            SingleMovieStream stream = new SingleMovieStream();
            stream.setFileName(fileName); // Sử dụng fileName từ payload (đã được makeUnique ở producer)
            stream.setFileUrl(fileUrl);   // Sử dụng fileUrl từ payload (đã được tạo ở producer)
            singleMovieStreamRepository.save(stream);
            logger.info("Created SingleMovieStream from Kafka: {}", stream);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error handling UPLOAD: {}. Error: {}", payload, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error handling UPLOAD: {}. Error: {}", payload, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during UPLOAD: " + e.getMessage(), e);
        }
    }

    private void handleDelete(Map<String, Object> payload) {
        try {
            Object fileIdObj = payload.get("fileId"); // Lấy giá trị fileId từ payload
            if (fileIdObj == null) {
                throw new IllegalArgumentException("fileId không được null");
            }

            // Chuyển đổi fileId thành Long một cách an toàn
            Long fileId;
            if (fileIdObj instanceof Number) {
                fileId = ((Number) fileIdObj).longValue(); // Chuyển đổi từ Integer hoặc Long
                logger.debug("Converted fileId {} to Long", fileId);
            } else {
                throw new IllegalArgumentException("fileId must be a number, found: " + fileIdObj.getClass().getName());
            }

            logger.info("Processing DELETE for fileId: {}", fileId);

            Optional<SingleMovieStream> streamOpt = singleMovieStreamRepository.findById(fileId);
            if (streamOpt.isPresent()) {
                SingleMovieStream stream = streamOpt.get();
                String fileName = stream.getFileName();

                // Xóa file trên GCS
                Blob blob = storage.get(bucketName, fileName);
                if (blob != null) {
                    storage.delete(bucketName, fileName);
                    logger.info("Deleted file from GCS: {}", fileName);
                } else {
                    logger.warn("File {} not found in GCS for fileId: {}", fileName, fileId);
                }

                // Xóa bản ghi trong database
                singleMovieStreamRepository.delete(stream);
                logger.info("Deleted SingleMovieStream from database for fileId: {}", fileId);
            } else {
                logger.warn("No SingleMovieStream found for fileId: {}. Skipping delete.", fileId);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error handling DELETE: {}. Error: {}", payload, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error handling DELETE: {}. Error: {}", payload, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during DELETE: " + e.getMessage(), e);
        }
    }

    private void sendToDlq(String messageJson) {
        try {
            // Gửi message vào DLQ đồng bộ
            kafkaTemplate.send(DLQ_TOPIC, messageJson).get(); // Chặn thread để chờ gửi xong
            logger.info("Successfully sent to DLQ: {}", messageJson);
        } catch (InterruptedException e) {
            logger.error("Interrupted while sending to DLQ: {}. Error: {}. Restoring interrupt status.", messageJson, e.getMessage());
            Thread.currentThread().interrupt(); // Khôi phục trạng thái gián đoạn
        } catch (ExecutionException e) {
            logger.error("Execution error while sending to DLQ: {}. Cause: {}", messageJson, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while sending to DLQ: {}. Error: {}", messageJson, e.getMessage());
        }
    }
}