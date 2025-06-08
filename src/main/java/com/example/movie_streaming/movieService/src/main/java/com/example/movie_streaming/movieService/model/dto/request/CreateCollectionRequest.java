package com.example.movie_streaming.movieService.model.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class CreateCollectionRequest {
    private String name;
    private Boolean featured;
    private Set<Long> movieIds;
}
