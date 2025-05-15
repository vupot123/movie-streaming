package com.example.movie_streaming.movieService.kafka;

import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;
    private static final String DLQ_TOPIC = "movie-topic-dlq";

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate, MovieRepository movieRepository, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "movie-topic", groupId = "movie-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String message = record.value();
        try {
            logger.info(" Received from Kafka: {}", message);

            if (message.startsWith("CREATE_MOVIE:")) {
                String movieJson = message.replace("CREATE_MOVIE:", "");
                Movie movie = objectMapper.readValue(movieJson, Movie.class);
                movie.setId(null); 
                movieRepository.save(movie);
                logger.info("Created movie from Kafka: {}", movie);
            } else if (message.startsWith("UPDATE_MOVIE:")) {
                String movieJson = message.replace("UPDATE_MOVIE:", "");
                Movie movie = objectMapper.readValue(movieJson, Movie.class);
                movieRepository.findById(movie.getId()).ifPresent(existing -> {
                    existing.setTitle(movie.getTitle());
                    existing.setType(movie.getType());
                    existing.setYear(movie.getYear());
                    existing.setDuration(movie.getDuration());
                    existing.setIntro(movie.getIntro());
                    existing.setAgeRating(movie.getAgeRating());
                    existing.setViews(movie.getViews());
                    movieRepository.save(existing);
                    logger.info("Updated movie from Kafka: {}", existing);
                });
            } else if (message.startsWith("DELETE_MOVIE:")) {
                String idStr = message.replace("DELETE_MOVIE:", "");
                Long id = Long.parseLong(idStr);
                movieRepository.deleteById(id);
                logger.info("Deleted movie with ID from Kafka: {}", id);
            } else if (message.startsWith("VIEW_MOVIE:")) {
                String movieJson = message.replace("VIEW_MOVIE:", "");
                Movie movie = objectMapper.readValue(movieJson, Movie.class);
                movieRepository.findById(movie.getId()).ifPresent(existing -> {
                    existing.setViews(movie.getViews());
                    movieRepository.save(existing);
                    logger.info("Updated views for movie from Kafka: {}", existing);
                });
            } else {
                logger.warn("Unknown message format: {}", message);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error(" Error processing message: {}. Error: {}", message, e.getMessage(), e);
            kafkaTemplate.send(DLQ_TOPIC, message);
        }
    }
}