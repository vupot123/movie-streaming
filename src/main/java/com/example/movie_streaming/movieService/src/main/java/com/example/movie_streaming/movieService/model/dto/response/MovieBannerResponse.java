package com.example.movie_streaming.movieService.model.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieBannerResponse {
    private Long movieId;
    private String smallBannerUrl;
    private String largeBannerUrl;
}
