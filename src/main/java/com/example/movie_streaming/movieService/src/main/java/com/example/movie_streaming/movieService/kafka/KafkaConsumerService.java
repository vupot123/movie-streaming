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
        log.info(" Received from Kafka topic movie-topic, partition={}, offset={}: {}",
                record.partition(), record.offset(), message);

        try {
            // Xử lý message
            if ("movie".equals(message.getEntityType())) {
                if (message.getAction() == null) {
                    throw new IllegalArgumentException("Action is null");
                }
                handleMovieMessage(message);
            } else {
                log.warn("Unknown entityType: {}", message.getEntityType());
            }

            // Commit offset
            acknowledgment.acknowledge();
            log.debug("Committed offset for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error(" Error processing message: {}. Sending to DLQ: {}", message, DLQ_TOPIC, e);
            kafkaTemplate.send(DLQ_TOPIC, message);
            acknowledgment.acknowledge();
            log.debug("Committed offset after error for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        }
    }

    protected void handleMovieMessage(KafkaMessage message) {
        String action = message.getAction();
        Long entityId = message.getEntityId();
        long startTime = System.currentTimeMillis();
        log.info("Processing action={} for movie, entityId={}", action, entityId);

        try {
            if (message.getPayload() == null && !"DELETE".equals(action)) {
                throw new IllegalArgumentException("Payload is null for action: " + action);
            }
            if (entityId == null && ("UPDATE".equals(action) || "DELETE".equals(action) || "VIEW".equals(action))) {
                throw new IllegalArgumentException("EntityId is null for action: " + action);
            }

            switch (action) {
                case "CREATE":
                    CreateMovieRequest createRequest = new CreateMovieRequest();
                    createRequest.setTitle((String) message.getPayload().get("title"));
                    createRequest.setType((String) message.getPayload().get("type"));
                    createRequest.setYear((Integer) message.getPayload().get("year"));
                    createRequest.setDuration((Integer) message.getPayload().get("duration"));
                    createRequest.setIntro((String) message.getPayload().get("intro"));
                    createRequest.setAgeRating((String) message.getPayload().get("ageRating"));
                    createRequest.setViews(((Number) message.getPayload().get("views")).longValue());
                    createRequest.setActorIds((List<Long>) message.getPayload().get("actorIds"));
                    createRequest.setGenreIds((List<Integer>) message.getPayload().get("genreIds"));
                    createRequest.setCountryIds((List<Integer>) message.getPayload().get("countryIds"));
                    createRequest.setTrailerUrls((List<String>) message.getPayload().get("trailerUrls"));
                    createRequest.setSmallBanner((String) message.getPayload().get("smallBanner"));
                    createRequest.setLargeBanner((String) message.getPayload().get("largeBanner"));
                    movieService.createMovie(createRequest);
                    break;

                case "UPDATE":
                    UpdateMovieRequest updateRequest = new UpdateMovieRequest();
                    updateRequest.setTitle((String) message.getPayload().get("title"));
                    updateRequest.setType((String) message.getPayload().get("type"));
                    updateRequest.setYear((Integer) message.getPayload().get("year"));
                    updateRequest.setDuration((Integer) message.getPayload().get("duration"));
                    updateRequest.setIntro((String) message.getPayload().get("intro"));
                    updateRequest.setAgeRating((String) message.getPayload().get("ageRating"));
                    updateRequest.setViews(((Number) message.getPayload().get("views")).longValue());
                    updateRequest.setActorIds((List<Long>) message.getPayload().get("actorIds"));
                    updateRequest.setGenreIds((List<Integer>) message.getPayload().get("genreIds"));
                    updateRequest.setCountryIds((List<Integer>) message.getPayload().get("countryIds"));
                    updateRequest.setTrailerUrls((List<String>) message.getPayload().get("trailerUrls"));
                    updateRequest.setSmallBanner((String) message.getPayload().get("smallBanner"));
                    updateRequest.setLargeBanner((String) message.getPayload().get("largeBanner"));
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
            log.error("Error processing action={} for movie, entityId={}: {}",
                    action, entityId, e.getMessage(), e);
            throw e;
        }
    }
}