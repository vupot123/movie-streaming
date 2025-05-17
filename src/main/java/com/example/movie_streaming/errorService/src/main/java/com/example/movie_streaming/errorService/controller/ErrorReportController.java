package com.example.movie_streaming.errorService.controller;

import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.service.ErrorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/error-reports")
@RequiredArgsConstructor
public class ErrorReportController {

    private final ErrorReportService errorReportService;

    @PostMapping("/{movieId}")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> createReport(
            @PathVariable("movieId") Long movieId,
            @RequestBody CreateErrorReportRequest request
    ) {
        try {
            ErrorReportResponse response = errorReportService.createReport(movieId, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Created report successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to create report: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ErrorReportResponse>>> getAllReports() {
        try {
            List<ErrorReportResponse> allReports = errorReportService.getAllReports();
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched reports successfully", allReports));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch report: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam ErrorStatus status
    ) {
        try {
            ErrorReportResponse updated = errorReportService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(200, "Updated report successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to update banner: " + e.getMessage(), null));
        }
    }
}
