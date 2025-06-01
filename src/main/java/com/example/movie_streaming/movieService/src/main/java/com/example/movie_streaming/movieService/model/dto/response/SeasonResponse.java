package com.example.movie_streaming.movieService.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResponse {
    private Long id;
    private Long movieId;
    private Integer seasonNumber;
    private String name;
    private Set<EpisodeResponse> episodes; // Đổi từ List sang Set
}