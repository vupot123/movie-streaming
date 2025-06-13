package com.example.movie_streaming.movieService.model.dto.request;


import lombok.Data;

@Data
public class UpdateFeaturedCollectionRequest {
    private Long id;
    private Boolean featured;
}

