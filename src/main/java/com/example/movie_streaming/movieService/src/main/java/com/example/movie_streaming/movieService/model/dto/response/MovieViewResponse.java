package com.example.movie_streaming.movieService.model.dto.response;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieViewResponse {
    private Long id;
    private Long movieId;
    private Long userId;
    private Timestamp viewedAt;
}
