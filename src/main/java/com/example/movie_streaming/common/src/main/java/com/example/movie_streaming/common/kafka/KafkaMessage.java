package com.example.movie_streaming.common.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KafkaMessage {

    @JsonProperty("entity_type")
    private String entityType;

    @JsonProperty("action")
    private String action;

    @JsonProperty("entity_id")
    private Long entityId;

    @JsonProperty("payload")
    private Map<String, Object> payload;
}