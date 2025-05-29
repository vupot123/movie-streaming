package com.example.movie_streaming.movieService.model.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CollectionResponse {
    private Long id;
    private String name;
    private Boolean featured;
    private List<Long> movieIds; // Danh sách ID phim trong bộ sưu tập
}