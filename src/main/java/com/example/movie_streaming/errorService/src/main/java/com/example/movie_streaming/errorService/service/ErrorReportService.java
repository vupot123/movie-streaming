package com.example.movie_streaming.errorService.service;

import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.repository.ErrorReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorReportService {

    private final ErrorReportRepository errorReportRepository;

    public ErrorReportResponse createReport(Long movieId, CreateErrorReportRequest request) {
        ErrorReport report = ErrorReport.builder()
                .movieId(movieId)
                .issue(request.getIssue())
                .status(ErrorStatus.UNCHECKED)
                .build();

        return toResponse(errorReportRepository.save(report));
    }

    public List<ErrorReportResponse> getAllReports() {
        return errorReportRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ErrorReportResponse updateStatus(Long id, ErrorStatus status) {
        ErrorReport report = errorReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error report not found"));

        report.setStatus(status);
        return toResponse(errorReportRepository.save(report));
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
}
