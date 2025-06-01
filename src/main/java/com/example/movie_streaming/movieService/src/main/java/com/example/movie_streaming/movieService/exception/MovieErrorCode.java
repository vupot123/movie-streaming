package com.example.movie_streaming.movieService.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MovieErrorCode {
    MOVIE_NOT_FOUND(1000, "Movie not found", 404),
    MOVIE_CREATION_FAILED(1001, "Failed to create movie", 500),
    MOVIE_UPDATE_FAILED(1002, "Failed to update movie", 500),
    MOVIE_DELETE_FAILED(1003, "Failed to delete movie", 500),
    MOVIE_FETCH_FAILED(1004, "Failed to fetch movie", 500),
    MOVIE_LIST_FETCH_FAILED(1005, "Failed to fetch movies", 500),
    MOVIE_ADD_VIEW_FAILED(1006, "Failed to add view", 500),
    MOVIE_FILTER_FAILED(1007, "Failed to filter movies", 500),
    MOVIE_SEARCH_FAILED(1008, "Search failed", 500);

    private final int code;
    private final String message;
    private final int httpStatus;
}
