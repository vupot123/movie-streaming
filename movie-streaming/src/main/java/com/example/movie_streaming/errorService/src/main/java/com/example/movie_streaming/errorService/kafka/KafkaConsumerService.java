package com.example.movie_streaming.errorService.kafka;

import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.repository.ErrorReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final ErrorReportRepository errorReportRepository;

    @KafkaListener(topics = "error-report-topic", groupId = "error-group")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            String json = record.value();

            KafkaMessage message = objectMapper.readValue(json, KafkaMessage.class);

            if ("error_report".equalsIgnoreCase(message.getEntityType())
                    && "CREATE".equalsIgnoreCase(message.getAction())) {

                Map<String, Object> payload = message.getPayload();

                Long movieId = Long.parseLong(payload.get("movieId").toString());
                String issue = payload.get("issue").toString();
                ErrorStatus status = ErrorStatus.valueOf(payload.get("status").toString());

                LocalDateTime createdAt = LocalDateTime.parse(payload.get("createdAt").toString());

                ErrorReport report = ErrorReport.builder()
                        .movieId(movieId)
                        .issue(issue)
                        .status(status)
                        .createdAt(createdAt)
                        .build();

                errorReportRepository.save(report);

                System.out.println("✅ Saved error report from Kafka for movieId = " + movieId);
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing Kafka message: " + e.getMessage());
        }
    }
}
