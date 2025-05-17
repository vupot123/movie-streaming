package com.example.movie_streaming.movieService.model.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleMovieStreamResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
}

