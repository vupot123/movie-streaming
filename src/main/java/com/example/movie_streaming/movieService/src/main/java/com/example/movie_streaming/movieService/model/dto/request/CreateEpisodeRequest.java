package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEpisodeRequest {
    private Integer episodeNumber;
    private String dubbed;
    private String subbed;
}
