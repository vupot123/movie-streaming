package com.example.movie_streaming.movieService.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeResponse {
    private Long id;
    private Long seasonId;
    private Integer episodeNumber;
    private String dubbedUrl;
    private String subtitleUrl;
}