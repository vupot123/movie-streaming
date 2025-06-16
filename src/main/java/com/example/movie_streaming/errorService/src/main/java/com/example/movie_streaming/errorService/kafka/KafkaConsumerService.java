package com.example.movie_streaming.errorService.kafka;

import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.repository.ErrorReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final ErrorReportRepository errorReportRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(ErrorReportRepository errorReportRepository, ObjectMapper objectMapper) {
        this.errorReportRepository = errorReportRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "error-report-topic", groupId = "error-report-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String messageJson = record.value();
        try {
            logger.info("📥 Nhận từ Kafka: {}", messageJson);

            KafkaMessage message = objectMapper.readValue(messageJson, KafkaMessage.class);
            Map<String, Object> payload = message.getPayload();

            if ("error-report".equals(message.getEntityType())) {
                switch (message.getAction()) {
                    case "CREATE":
                        handleCreate(payload);
                        break;
                    case "UPDATE":
                        handleUpdate(message.getEntityId(), payload);
                        break;
                    case "DELETE":
                        handleDelete(message.getEntityId());
                        break;
                    case "GET_ALL":
                        handleGetAll(); // Có thể bỏ qua nếu không cần logic đặc biệt
                        break;
                    default:
                        logger.warn("Hành động không xác định: {}", message.getAction());
                }
            } else {
                logger.warn("Loại thực thể không xác định: {}", message.getEntityType());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý thông điệp: {}. Lỗi: {}", messageJson, e.getMessage(), e);
        }
    }

    private void handleCreate(Map<String, Object> payload) {
        logger.info("Xử lý CREATE với payload: {}", payload);
        try {
            ErrorReportResponse response = objectMapper.convertValue(payload, ErrorReportResponse.class);
            ErrorReport report = ErrorReport.builder()
                    .movieId(response.getMovieId())
                    .issue(response.getIssue())
                    .status(response.getStatus())
                    .createdAt(response.getCreatedAt())
                    .build();
            errorReportRepository.save(report);
            logger.info("Đã lưu báo cáo lỗi với ID: {}", report.getId());
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý CREATE: {}", e.getMessage(), e);
        }
    }

    private void handleUpdate(Long id, Map<String, Object> payload) {
        logger.info("Xử lý UPDATE với ID: {}, payload: {}", id, payload);
        try {
            ErrorReport report = errorReportRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Báo cáo lỗi không tồn tại"));
            ErrorReportResponse response = objectMapper.convertValue(payload, ErrorReportResponse.class);
            report.setStatus(response.getStatus());
            errorReportRepository.save(report);
            logger.info("Đã cập nhật báo cáo lỗi với ID: {}", id);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý UPDATE cho ID {}: {}", id, e.getMessage(), e);
        }
    }

    private void handleDelete(Long id) {
        logger.info("Xử lý DELETE với ID: {}", id);
        try {
            errorReportRepository.deleteById(id);
            logger.info("Đã xóa báo cáo lỗi với ID: {}", id);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý DELETE cho ID {}: {}", id, e.getMessage(), e);
        }
    }

    private void handleGetAll() {
        logger.info("Xử lý GET_ALL: Lấy tất cả báo cáo lỗi (phân trang và lọc đã xử lý trong service)");
    }

    private static class KafkaMessage {
        private String entityType;
        private String action;
        private Long entityId;
        private Map<String, Object> payload;

        public String getEntityType() { return entityType; }
        public String getAction() { return action; }
        public Long getEntityId() { return entityId; }
        public Map<String, Object> getPayload() { return payload; }
    }
}