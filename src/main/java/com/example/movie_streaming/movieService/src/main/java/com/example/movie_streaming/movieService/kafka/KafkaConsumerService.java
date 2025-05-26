package com.example.movie_streaming.movieService.kafka;

import com.example.movie_streaming.common.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
public class KafkaConsumerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;
    private static final String DLQ_TOPIC = "movie-topic-dlq";

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate,
                                MovieRepository movieRepository,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "movie-topic", groupId = "movie-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        long startTime = System.currentTimeMillis();
        log.info("Received from Kafka topic movie-topic, partition={}, offset={}: {}",
                record.partition(), record.offset(), messageJson);

        try {
            // Parse JSON thành KafkaMessage
            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            log.debug("Parsed KafkaMessage: {}", message);

            // Xử lý message
            if ("movie".equals(message.getEntityType())) {
                if (message.getAction() == null) {
                    throw new IllegalArgumentException("Action is null");
                }
                handleMovieMessageAsync(message);
            } else {
                log.warn("Unknown entityType: {}", message.getEntityType());
            }

            // Commit offset
            acknowledgment.acknowledge();
            log.debug("Committed offset for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Error processing message: {}. Sending to DLQ: {}", messageJson, DLQ_TOPIC, e);
            kafkaTemplate.send(DLQ_TOPIC, messageJson);
            acknowledgment.acknowledge();
            log.debug("Committed offset after error for partition={}, offset={}, took {} ms",
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        }
    }

    @Async("asyncExecutor")
    protected void handleMovieMessageAsync(KafkaMessage message) {
        String action = message.getAction();
        Map<String, Object> payload = message.getPayload();
        Long entityId = message.getEntityId();
        long startTime = System.currentTimeMillis();
        log.info("Processing async action={} for movie, entityId={}", action, entityId);

        try {
            if (payload == null && !"DELETE".equals(action)) {
                throw new IllegalArgumentException("Payload is null for action: " + action);
            }
            if (entityId == null && ("UPDATE".equals(action) || "DELETE".equals(action) || "VIEW".equals(action))) {
                throw new IllegalArgumentException("EntityId is null for action: " + action);
            }

            switch (action) {
                case "CREATE":
                    createMovie(payload);
                    break;
                case "UPDATE":
                    updateMovie(payload);
                    break;
                case "DELETE":
                    deleteMovie(entityId);
                    break;
                case "VIEW":
                    updateViews(payload);
                    break;
                default:
                    log.warn("Unknown action: {}", action);
            }
            log.info("Completed async action={} for movie, took {} ms",
                    action, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Error processing async action={} for movie, entityId={}: {}",
                    action, entityId, e.getMessage(), e);

        }
    }

    @Transactional
    protected void createMovie(Map<String, Object> payload) {
        Movie movie = objectMapper.convertValue(payload, Movie.class);
        movie.setId(null); // Đảm bảo tạo mới
        movieRepository.save(movie);
        log.info("Created movie: {}", movie);
    }

    @Transactional
    protected void updateMovie(Map<String, Object> payload) {
        Movie movie = objectMapper.convertValue(payload, Movie.class);
        movieRepository.findById(movie.getId()).ifPresent(existing -> {
            existing.setTitle(movie.getTitle());
            existing.setType(movie.getType());
            existing.setYear(movie.getYear());
            existing.setDuration(movie.getDuration());
            existing.setIntro(movie.getIntro());
            existing.setAgeRating(movie.getAgeRating());
            existing.setViews(movie.getViews());
            movieRepository.save(existing);
            log.info("Updated movie: {}", existing);
        });
    }

    @Transactional
    protected void deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            log.info("Deleted movie with ID: {}", id);
        } else {
            log.warn("Movie with ID {} not found for deletion", id);
        }
    }

    @Transactional
    protected void updateViews(Map<String, Object> payload) {
        Movie movie = objectMapper.convertValue(payload, Movie.class);
        movieRepository.findById(movie.getId()).ifPresent(existing -> {
            existing.setViews(movie.getViews());
            movieRepository.save(existing);
            log.info("Updated views for movie: {}", existing);
        });
    }
}