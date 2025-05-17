package com.example.movie_streaming.movieService.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieTrailerResponse {
    private Long id;
    private Long movieId;
    private String url;
}
