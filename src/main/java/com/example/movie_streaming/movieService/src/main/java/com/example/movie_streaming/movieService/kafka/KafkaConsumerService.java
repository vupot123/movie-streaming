package com.example.movie_streaming.movieService.kafka;

import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    private final KafkaTemplate<String, KafkaMessage> kafkaTemplate;
    private final MovieService movieService;
    private static final String DLQ_TOPIC = "movie-topic-dlq";

    public KafkaConsumerService(KafkaTemplate<String, KafkaMessage> kafkaTemplate,
                                MovieService movieService) {
        this.kafkaTemplate = kafkaTemplate;
        this.movieService = movieService;
    }

    @KafkaListener(topics = "movie-topic", groupId = "movie-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, KafkaMessage> record, Acknowledgment acknowledgment) {
        KafkaMessage message = record.value();
        long startTime = System.currentTimeMillis();
        log.info("📥 Received from Kafka topic movie-topic, partition={}, offset={}: {}",
                record.partition(), record.offset(), message);

        if (message == null) {
            log.warn("Received null message, skipping partition={}, offset={}", record.partition(), record.offset());
            acknowledgment.acknowledge();
            return;
        }

        try {
            // Validate required fields
            if (message.getEntityType() == null) {
                throw new IllegalArgumentException("EntityType is null");
            }
            if ("movie".equals(message.getEntityType()) && message.getAction() == null) {
                throw new IllegalArgumentException("Action is null for movie entity");
            }

            // Xử lý message
            if ("movie".equals(message.getEntityType())) {
                handleMovieMessage(message);
            } else {
                log.warn("Unknown entityType: {}", message.getEntityType());
            }

            // Commit offset
            acknowledgment.acknowledge();
            log.debug("Committed offset for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("❌ Error processing message: {}. Sending to DLQ: {}", message, DLQ_TOPIC, e);
            if (message != null) {
                kafkaTemplate.send(DLQ_TOPIC, message);
            }
            acknowledgment.acknowledge(); // Commit dù lỗi để tránh lặp lại
            log.debug("Committed offset after error for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        }
    }

    protected void handleMovieMessage(KafkaMessage message) {
        String action = message.getAction();
        Long entityId = message.getEntityId();
        Map<String, Object> payload = message.getPayload();
        long startTime = System.currentTimeMillis();
        log.info("Processing action={} for movie, entityId={}", action, entityId);

        try {
            if (payload == null && !"DELETE".equals(action)) {
                throw new IllegalArgumentException("Payload is null for action: " + action);
            }
            if (entityId == null && ("UPDATE".equals(action) || "DELETE".equals(action) || "VIEW".equals(action))) {
                throw new IllegalArgumentException("EntityId is null for action: " + action);
            }

            switch (action) {
                case "CREATE":
                    CreateMovieRequest createRequest = new CreateMovieRequest();
                    createRequest.setTitle(getStringFromPayload(payload, "title"));
                    createRequest.setType(getStringFromPayload(payload, "type"));
                    createRequest.setYear(getIntegerFromPayload(payload, "year"));
                    createRequest.setDuration(getIntegerFromPayload(payload, "duration"));
                    createRequest.setIntro(getStringFromPayload(payload, "intro"));
                    createRequest.setAgeRating(getStringFromPayload(payload, "ageRating"));
                    createRequest.setViews(getLongFromPayload(payload, "views", 0L));
                    createRequest.setActorIds(getListLongFromPayload(payload, "actorIds"));
                    createRequest.setGenreNames(getListStringFromPayload(payload, "genreName"));
                    createRequest.setCountryName(getStringFromPayload(payload, "countryName"));
                    createRequest.setSmallBanner(getStringFromPayload(payload, "smallBanner"));
                    createRequest.setLargeBanner(getStringFromPayload(payload, "largeBanner"));
                    movieService.createMovie(createRequest);
                    break;

                case "UPDATE":
                    UpdateMovieRequest updateRequest = new UpdateMovieRequest();
                    updateRequest.setTitle(getStringFromPayload(payload, "title"));
                    updateRequest.setType(getStringFromPayload(payload, "type"));
                    updateRequest.setYear(getIntegerFromPayload(payload, "year"));
                    updateRequest.setDuration(getIntegerFromPayload(payload, "duration"));
                    updateRequest.setIntro(getStringFromPayload(payload, "intro"));
                    updateRequest.setAgeRating(getStringFromPayload(payload, "ageRating"));
                    updateRequest.setViews(getLongFromPayload(payload, "views", 0L));
                    updateRequest.setActorIds(getListLongFromPayload(payload, "actorIds"));
                    updateRequest.setGenreNames(getListStringFromPayload(payload, "genreName"));
                    updateRequest.setCountryName(getStringFromPayload(payload, "countryName"));
                    updateRequest.setSmallBanner(getStringFromPayload(payload, "smallBanner"));
                    updateRequest.setLargeBanner(getStringFromPayload(payload, "largeBanner"));
                    movieService.updateMovie(entityId, updateRequest);
                    break;

                case "DELETE":
                    movieService.deleteMovie(entityId);
                    break;

                case "VIEW":
                    movieService.addView(entityId);
                    break;

                default:
                    log.warn("Unknown action: {}", action);
            }
            log.info("Completed action={} for movie, took {} ms",
                    action, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("❌ Error processing action={} for movie, entityId={}: {}",
                    action, entityId, e.getMessage(), e);
            throw e; // Để transaction rollback
        }
    }

    // Helper methods to safely get values from payload
    private String getStringFromPayload(Map<String, Object> payload, String key) {
        return payload != null && payload.containsKey(key) ? String.valueOf(payload.get(key)) : null;
    }

    private Integer getIntegerFromPayload(Map<String, Object> payload, String key) {
        return payload != null && payload.containsKey(key) ? ((Number) payload.get(key)).intValue() : null;
    }

    private Long getLongFromPayload(Map<String, Object> payload, String key, Long defaultValue) {
        return payload != null && payload.containsKey(key) ? ((Number) payload.get(key)).longValue() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getListLongFromPayload(Map<String, Object> payload, String key) {
        return payload != null && payload.containsKey(key) ? (List<Long>) payload.get(key) : null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getListStringFromPayload(Map<String, Object> payload, String key) {
        return payload != null && payload.containsKey(key) ? (List<String>) payload.get(key) : null;
    }
}