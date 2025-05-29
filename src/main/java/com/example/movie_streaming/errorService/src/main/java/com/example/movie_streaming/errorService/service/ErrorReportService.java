package com.example.movie_streaming.errorService.service;

import com.example.movie_streaming.errorService.kafka.KafkaMessage;
import com.example.movie_streaming.errorService.kafka.KafkaProducerService;
import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ErrorReportService {

    private final KafkaProducerService kafkaProducerService;

    private static final String TOPIC = "error-report-topic";

    public ErrorReportResponse createReport(Long movieId, CreateErrorReportRequest request) {
        // Tạo message để gửi đi
        Map<String, Object> payload = new HashMap<>();
        payload.put("movieId", movieId);
        payload.put("issue", request.getIssue());
        payload.put("status", ErrorStatus.UNCHECKED.name());
        payload.put("createdAt", LocalDateTime.now().toString());

        KafkaMessage message = new KafkaMessage(
                "error_report",
                "CREATE",
                null,
                payload
        );

        kafkaProducerService.sendMessage(TOPIC, message.toString());

        // Trả response mô phỏng (vì chưa có id thực do chưa lưu DB)
        return new ErrorReportResponse(
                null, // id tạm null
                movieId,
                request.getIssue(),
                ErrorStatus.UNCHECKED,
                LocalDateTime.now()
        );
    }

    public List<ErrorReportResponse> getAllReports() {
        throw new UnsupportedOperationException("This service now only sends data via Kafka");
    }

    public ErrorReportResponse updateStatus(Long id, ErrorStatus status) {
        throw new UnsupportedOperationException("Update now handled by downstream consumer via Kafka");
    }
}


