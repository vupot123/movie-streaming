package com.example.movie_streaming.movieService.model.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieBannerResponse {
    private Long id;
    private Long movieId;
    private String smallBanner;
    private String largeBanner;
}
