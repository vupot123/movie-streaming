package com.example.movie_streaming.errorService.service;

import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.repository.ErrorReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorReportService {

    private final ErrorReportRepository errorReportRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String ERROR_REPORT_TOPIC = "error-report-topic";

    public ErrorReportResponse createReport(Long movieId, CreateErrorReportRequest request) {
        ErrorReport report = ErrorReport.builder()
                .movieId(movieId)
                .issue(request.getIssue())
                .status(ErrorStatus.UNCHECKED)
                .build();

        ErrorReport savedReport = errorReportRepository.save(report);
        sendKafkaMessage("CREATE", savedReport);
        return toResponse(savedReport);
    }

    public List<ErrorReportResponse> getAllReports() {
        List<ErrorReport> reports = errorReportRepository.findAll();
        sendKafkaMessage("GET_ALL", null); // Gửi thông điệp GET_ALL
        return reports.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ErrorReportResponse updateStatus(Long id, ErrorStatus status) {
        ErrorReport report = errorReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo lỗi không tồn tại"));

        report.setStatus(status);
        ErrorReport updatedReport = errorReportRepository.save(report);
        sendKafkaMessage("UPDATE", updatedReport);
        return toResponse(updatedReport);
    }

    public void deleteReport(Long id) {
        ErrorReport report = errorReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo lỗi không tồn tại"));

        errorReportRepository.delete(report);
        sendKafkaMessage("DELETE", report);
    }

    private void sendKafkaMessage(String action, ErrorReport report) {
        try {
            // Tạo payload cho Kafka
            var payload = new KafkaMessage(
                    "error-report",
                    action,
                    report != null ? report.getId() : null,
                    report != null ? toResponse(report) : null
            );
            String messageJson = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(ERROR_REPORT_TOPIC, messageJson);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi thông điệp Kafka: " + e.getMessage(), e);
        }
    }

    private ErrorReportResponse toResponse(ErrorReport entity) {
        return new ErrorReportResponse(
                entity.getId(),
                entity.getMovieId(),
                entity.getIssue(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    // Định nghĩa lớp KafkaMessage bên trong để gửi thông điệp
    private static class KafkaMessage {
        private String entityType;
        private String action;
        private Long entityId;
        private Object payload;

        public KafkaMessage(String entityType, String action, Long entityId, Object payload) {
            this.entityType = entityType;
            this.action = action;
            this.entityId = entityId;
            this.payload = payload;
        }

        // Getters
        public String getEntityType() { return entityType; }
        public String getAction() { return action; }
        public Long getEntityId() { return entityId; }
        public Object getPayload() { return payload; }
    }
}
