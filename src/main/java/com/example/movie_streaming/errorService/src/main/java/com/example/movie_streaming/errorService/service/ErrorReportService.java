package com.example.movie_streaming.errorService.service;

import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.repository.ErrorReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorReportService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorReportService.class); // Khai báo Logger

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
        sendKafkaMessage("CREATE", report);
        return toResponse(report);
    }

    public Page<ErrorReportResponse> getAllReports(int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ErrorReport> reports;
        if (status != null) {
            ErrorStatus errorStatus = status == 0 ? ErrorStatus.UNCHECKED : ErrorStatus.CHECKED;
            reports = errorReportRepository.findByStatus(errorStatus, pageable); // Cần cập nhật repository
        } else {
            reports = errorReportRepository.findAll(pageable);
        }
        sendKafkaMessage("GET_ALL", null);
        return reports.map(this::toResponse);
    }

    public ErrorReportResponse updateStatus(Long id, ErrorStatus status) {
        ErrorReport report = ErrorReport.builder()
                .id(id)
                .status(status)
                .build();
        sendKafkaMessage("UPDATE", report);
        return toResponse(report);
    }

    public void deleteReport(Long id) {
        ErrorReport report = ErrorReport.builder()
                .id(id)
                .build();
        sendKafkaMessage("DELETE", report);
    }

    // Thêm phương thức getReportById
    public ErrorReportResponse getReportById(Long id) {
        ErrorReport report = errorReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo lỗi không tồn tại"));
        sendKafkaMessage("GET", report); // Gửi thông điệp GET
        return toResponse(report);
    }

    private void sendKafkaMessage(String action, ErrorReport report) {
        try {
            var payload = report != null ? toResponse(report) : null;
            var message = new KafkaMessage("error-report", action, report != null ? report.getId() : null, payload);
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(ERROR_REPORT_TOPIC, messageJson);
            logger.debug("Đã gửi thông điệp Kafka: action={}, payload={}", action, messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông điệp Kafka: {}", e.getMessage(), e);
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