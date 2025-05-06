package com.example.movie_streaming.movieService.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String entityType; // user, movie, series...
    private String action;     // CREATE, UPDATE, DELETE
    private Long entityId;
    private Map<String, Object> payload;
}
