package com.example.movie_streaming.movieService.model.entity;

public enum MovieType {
    LE, BO;

    public static MovieType fromString(String type) {
        return valueOf(type.toUpperCase());
    }
}