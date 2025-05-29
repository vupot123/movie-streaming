package com.example.movie_streaming.errorService.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateErrorStatusRequest {
    private Long movieId;
    private String issue;
}
