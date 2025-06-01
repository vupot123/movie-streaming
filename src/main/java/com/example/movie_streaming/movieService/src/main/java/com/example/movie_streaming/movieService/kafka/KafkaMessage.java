package com.example.movie_streaming.movieService.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    @JsonProperty("entityType")
    private String entityType; // user, movie, series...

    @JsonProperty("action")
    private String action; // CREATE, UPDATE, DELETE, VIEW

    @JsonProperty("entityId")
    private Long entityId;

    @JsonProperty("payload")
    private Map<String, Object> payload;

    @Override
    public String toString() {
        return "KafkaMessage{" +
                "entityType='" + entityType + '\'' +
                ", action='" + action + '\'' +
                ", entityId=" + entityId +
                ", payload=" + payload +
                '}';
    }
}