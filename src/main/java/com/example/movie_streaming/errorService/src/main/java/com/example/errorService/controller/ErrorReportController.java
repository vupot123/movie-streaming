package com.example.errorService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.errorService.model.dto.response.ErrorReportResponse;
import com.example.errorService.model.entity.ErrorStatus;
import com.example.errorService.service.ErrorReportService;
import lombok.RequiredArgsConstructor;
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
            @PathVariable Long movieId,
            @RequestBody CreateErrorReportRequest request
    ) {
        try {
            ErrorReportResponse response = errorReportService.createReport(movieId, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Tạo báo lỗi thành công", response));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Không thể tạo báo lỗi cho movieId: " + movieId);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ErrorReportResponse>>> getAllReports() {
        try {
            List<ErrorReportResponse> allReports = errorReportService.getAllReports();
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách báo lỗi thành công", allReports));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Không thể lấy danh sách báo lỗi");
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam ErrorStatus status
    ) {
        try {
            ErrorReportResponse updated = errorReportService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", updated));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Không thể cập nhật trạng thái báo lỗi ID: " + id);
        }
    }
}
