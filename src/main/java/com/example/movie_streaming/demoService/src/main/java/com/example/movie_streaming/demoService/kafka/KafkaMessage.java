package com.example.movie_streaming.demoService.kafka;

import java.util.Map;

public class KafkaMessage {
    private Long demoId;
    private String action;
    private Map<String, Object> updatedFields;

    // Constructor
    public KafkaMessage(Long demoId, String action, Map<String, Object> updatedFields) {
        this.demoId = demoId;
        this.action = action;
        this.updatedFields = updatedFields;
    }

    public Long getDemoId() {
        return demoId;
    }

    public void setDemoId(Long demoId) {
        this.demoId = demoId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getUpdatedFields() {
        return updatedFields;
    }

    public void setUpdatedFields(Map<String, Object> updatedFields) {
        this.updatedFields = updatedFields;
    }

    // Optional toString for easier logging
    @Override
    public String toString() {
        return "KafkaMessage{" +
                "demoId=" + demoId +
                ", action='" + action + '\'' +
                ", updatedFields=" + updatedFields +
                '}';
    }
}
