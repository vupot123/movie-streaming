package com.example.movie_streaming.movieService.model.dto.request;


import lombok.Data;

@Data
public class UpdateCollectionRequest {
    private Long id;
    private String name;
    private Boolean featured;
}

