package com.example.movie_streaming.movieService.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMovieTrailerRequest {
    @NotBlank(message = "Trailer URL cannot be blank")
    private String url;
}
