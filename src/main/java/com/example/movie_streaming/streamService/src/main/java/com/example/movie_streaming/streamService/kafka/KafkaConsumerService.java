//package com.example.movie_streaming.streamService.kafka;
//
//import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
//import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.cloud.storage.Blob;
//import com.google.cloud.storage.Storage;
//import jakarta.transaction.Transactional;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ExecutionException;
//
//@Service
//public class KafkaConsumerService {
//
//    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
//    private static final String DLQ_TOPIC = "file-uploaded-topic-dlq"; // Sử dụng DLQ duy nhất
//
//    private final KafkaTemplate<String, String> kafkaTemplate;
//    private final SingleMovieStreamRepository singleMovieStreamRepository;
//    private final ObjectMapper objectMapper;
//    private final Storage storage;
//    @Value("${spring.cloud.gcp.storage.bucket}")
//    private String bucketName;
//
//    @Autowired
//    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate,
//                                SingleMovieStreamRepository singleMovieStreamRepository,
//                                ObjectMapper objectMapper,
//                                Storage storage) {
//        this.kafkaTemplate = kafkaTemplate;
//        this.singleMovieStreamRepository = singleMovieStreamRepository;
//        this.objectMapper = objectMapper;
//        this.storage = storage;
//    }
//
//    @KafkaListener(topics = "file-uploaded-topic", groupId = "file-uploaded-group", containerFactory = "kafkaListenerContainerFactory")
//    @Transactional
//    public void listen(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
//        String messageJson = consumerRecord.value();
//        try {
//            logger.info("Received message from Kafka: {}", messageJson);
//
//            // Parse JSON thành KafkaMessage
//            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
//            Map<String, Object> payload = message.getPayload();
//
//            if (payload == null) {
//                throw new IllegalArgumentException("Payload không được null");
//            }
//
//            String entityType = message.getEntityType();
//            String action = message.getAction();
//
//            if ("file-upload".equals(entityType)) {
//                switch (action) {
//                    case "DELETE":
//                        handleDelete(payload);
//                        break;
//                    case "GET":
//                    case "GET_ALL":
//                        logger.info("Received {} action for payload: {}. Ignoring as direct DB query is used.", message.getAction(), payload);
//                        break;
//                    default:
//                        logger.warn("Unsupported action: {}. Ignoring message: {}", message.getAction(), messageJson);
//                        throw new IllegalArgumentException("Hành động không hợp lệ: " + action);
//                }
//            } else {
//                logger.warn("Unsupported entity type: {}. Ignoring message: {}", entityType, messageJson);
//                throw new IllegalArgumentException("Loại thực thể không hợp lệ: " + entityType);
//            }
//
//            // Xác nhận message đã xử lý thành công
//            acknowledgment.acknowledge();
//        } catch (IllegalArgumentException e) {
//            logger.error("Validation error processing message: {}. Error: {}. Sending to DLQ.", messageJson, e.getMessage());
//            sendToDlq(messageJson);
//        } catch (Exception e) {
//            logger.error("Error processing message: {}. Error: {}. Sending to DLQ.", messageJson, e.getMessage(), e);
//            sendToDlq(messageJson);
//        }
//    }
//
//    private void handleDelete(Map<String, Object> payload) {
//        try {
//            Object fileIdObj = payload.get("fileId");
//            if (fileIdObj == null) {
//                throw new IllegalArgumentException("fileId không được null");
//            }
//
//            Long fileId;
//            if (fileIdObj instanceof Number) {
//                fileId = ((Number) fileIdObj).longValue();
//                logger.debug("Converted fileId {} to Long", fileId);
//            } else {
//                throw new IllegalArgumentException("fileId must be a number, found: " + fileIdObj.getClass().getName());
//            }
//
//            logger.info("Processing DELETE for fileId: {}", fileId);
//
//            Optional<SingleMovieStream> streamOpt = singleMovieStreamRepository.findById(fileId);
//            if (streamOpt.isPresent()) {
//                SingleMovieStream stream = streamOpt.get();
//                String fileName = stream.getFileName();
//
//                // Xóa file trên GCS
//                Blob blob = storage.get(bucketName, fileName);
//                if (blob != null) {
//                    storage.delete(bucketName, fileName);
//                    logger.info("Deleted file from GCS: {}", fileName);
//                } else {
//                    logger.warn("File {} not found in GCS for fileId: {}", fileName, fileId);
//                }
//
//                // Xóa bản ghi trong database
//                singleMovieStreamRepository.delete(stream);
//                logger.info("Deleted SingleMovieStream from database for fileId: {}", fileId);
//            } else {
//                logger.warn("No SingleMovieStream found for fileId: {}. Skipping delete.", fileId);
//            }
//        } catch (IllegalArgumentException e) {
//            logger.error("Validation error handling DELETE: {}. Error: {}", payload, e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            logger.error("Error handling DELETE: {}. Error: {}", payload, e.getMessage(), e);
//            throw new RuntimeException("Unexpected error during DELETE: " + e.getMessage(), e);
//        }
//    }
//
//    private void sendToDlq(String messageJson) {
//        try {
//            kafkaTemplate.send(DLQ_TOPIC, messageJson).get();
//            logger.info("Successfully sent to DLQ: {}", messageJson);
//        } catch (InterruptedException e) {
//            logger.error("Interrupted while sending to DLQ: {}. Error: {}. Restoring interrupt status.", messageJson, e.getMessage());
//            Thread.currentThread().interrupt();
//        } catch (ExecutionException e) {
//            logger.error("Execution error while sending to DLQ: {}. Cause: {}", messageJson, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error while sending to DLQ: {}. Error: {}", messageJson, e.getMessage());
//        }
//    }
//}