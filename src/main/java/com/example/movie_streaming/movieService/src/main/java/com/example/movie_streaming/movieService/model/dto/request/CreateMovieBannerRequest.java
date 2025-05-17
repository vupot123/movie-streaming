package com.example.movie_streaming.movieService.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieBannerRequest {
    @NotBlank
    private String smallBanner;

    @NotBlank
    private String largeBanner;
}

