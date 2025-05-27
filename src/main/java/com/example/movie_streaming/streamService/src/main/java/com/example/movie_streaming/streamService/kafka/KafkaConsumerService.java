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

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SingleMovieStreamRepository singleMovieStreamRepository;
    private final ObjectMapper objectMapper;
    private static final String DLQ_TOPIC = "file-uploaded-topic-dlq";

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
                    case "GET":
                    case "GET_ALL":
                        logger.info("Processed {} action for payload: {}", message.getAction(), payload);
                        break;
                    default:
                        logger.warn("Unsupported action: {}", message.getAction());
                        break;
                }
            } else {
                logger.warn("Unsupported entity type: {}", message.getEntityType());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing message: {}. Error: {}", messageJson, e.getMessage(), e);
            kafkaTemplate.send(DLQ_TOPIC, messageJson);
        }
    }

    private void handleUpload(Map<String, Object> payload) {
        try {
            Long movieId = Long.valueOf(payload.get("movieId").toString());
            String fileName = (String) payload.get("fileName");
            String fileUrl = (String) payload.get("fileUrl");
            String contentType = (String) payload.get("contentType");

            logger.info("Processing UPLOAD for movieId: {}, fileName: {}", movieId, fileName);

            if (singleMovieStreamRepository.findByFileName(fileName).isPresent()) {
                logger.warn("FileName {} already exists in database.", fileName);
                throw new IllegalArgumentException("File name already exists: " + fileName);
            }

            SingleMovieStream movieStream = new SingleMovieStream(movieId, fileName, fileUrl);
            singleMovieStreamRepository.save(movieStream);
            logger.info("Created SingleMovieStream from Kafka: {}", movieStream);
        } catch (NumberFormatException e) {
            logger.error("Invalid movieId format in payload: {}. Error: {}", payload, e.getMessage());
            throw new IllegalArgumentException("Invalid movieId format", e);
        }
    }

    private void handleDelete(Map<String, Object> payload) {
        try {
            Long movieId = Long.valueOf(payload.get("movieId").toString());
            String fileName = (String) payload.get("fileName");

            logger.info("Processing DELETE for movieId: {}, fileName: {}", movieId, fileName);

            Optional<SingleMovieStream> movieStream = singleMovieStreamRepository.findByMovieIdAndFileName(movieId, fileName);
            if (movieStream.isPresent()) {
                singleMovieStreamRepository.deleteByMovieIdAndFileName(movieId, fileName);
                logger.info("Deleted SingleMovieStream for movieId: {} and fileName: {}", movieId, fileName);
            } else {
                logger.warn("No SingleMovieStream found for movieId: {} and fileName: {}", movieId, fileName);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid movieId format in payload: {}. Error: {}", payload, e.getMessage());
            throw new IllegalArgumentException("Invalid movieId format", e);
        }
    }
}