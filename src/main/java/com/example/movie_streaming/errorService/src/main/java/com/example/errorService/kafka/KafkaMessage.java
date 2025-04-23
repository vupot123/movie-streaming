package com.example.errorService.kafka;

import java.util.Map;

public class KafkaMessage {
    private String entityType; // user, movie, series...
    private String action;     // CREATE, UPDATE, DELETE
    private Long entityId;
    private Map<String, Object> payload;

    // Constructors
    public KafkaMessage() {
    }

    public KafkaMessage(String entityType, String action, Long entityId, Map<String, Object> payload) {
        this.entityType = entityType;
        this.action = action;
        this.entityId = entityId;
        this.payload = payload;
    }

    // Getters and setters
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

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
