package com.example.movie_streaming.errorService.controller;

import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.errorService.model.dto.request.CreateErrorReportRequest;
import com.example.movie_streaming.errorService.model.dto.response.ErrorReportResponse;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import com.example.movie_streaming.errorService.service.ErrorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/error-reports")
@RequiredArgsConstructor
public class ErrorReportController {

    private final ErrorReportService errorReportService;

    @PostMapping("/{movieId}")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> createReport(
            @PathVariable("movieId") Long movieId,
            @RequestBody CreateErrorReportRequest request) {
        try {
            ErrorReportResponse response = errorReportService.createReport(movieId, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Tạo báo cáo lỗi thành công", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi tạo báo cáo: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ErrorReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ErrorReportResponse> reports = errorReportService.getAllReports(page, size, status);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách báo cáo lỗi thành công", reports));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi lấy báo cáo: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam ErrorStatus status) {
        try {
            ErrorReportResponse updated = errorReportService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái báo cáo thành công", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable("id") Long id) {
        try {
            errorReportService.deleteReport(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa báo cáo lỗi thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi xóa báo cáo: " + e.getMessage(), null));
        }
    }

    // Thêm phương thức GET bằng ID (Read)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ErrorReportResponse>> getReportById(@PathVariable("id") Long id) {
        try {
            ErrorReportResponse report = errorReportService.getReportById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy báo cáo lỗi thành công", report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi lấy báo cáo: " + e.getMessage(), null));
        }
    }
}