package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieTrailerRequest {
    private Long id;
    private Long movieId;
    private String url;
}
