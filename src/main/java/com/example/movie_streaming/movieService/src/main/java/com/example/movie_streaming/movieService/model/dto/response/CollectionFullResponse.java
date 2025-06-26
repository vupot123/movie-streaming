package com.example.movie_streaming.movieService.model.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionFullResponse {
    private Long id;
    private String name;
    private Boolean featured;
    private List<MovieResponse> movies;
}
