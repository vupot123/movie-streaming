package com.example.movie_streaming.errorService.model.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateErrorReportRequest {
    private String issue;
}
