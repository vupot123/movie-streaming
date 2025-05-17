package com.example.movie_streaming.movieService.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateMovieTrailerRequest {
    @NotNull(message = "Movie ID cannot be null")
    private Long movieId;
    @NotBlank(message = "Trailer URL cannot be blank")
    private String url;
}
