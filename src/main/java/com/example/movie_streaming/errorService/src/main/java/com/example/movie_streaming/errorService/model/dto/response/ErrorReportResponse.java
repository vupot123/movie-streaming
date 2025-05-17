package com.example.movie_streaming.errorService.model.dto.response;

import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorReportResponse {
    private Long id;
    private Long movieId;
    private String issue;
    private ErrorStatus status;
    private LocalDateTime createdAt;
}