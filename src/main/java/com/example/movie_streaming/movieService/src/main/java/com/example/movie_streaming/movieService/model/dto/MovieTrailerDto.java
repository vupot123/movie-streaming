package com.example.movie_streaming.movieService.model.dto;

public class MovieTrailerDto {

    private Long movieId;
    private String trailerUrl;

    // Constructor
    public MovieTrailerDto(Long movieId, String trailerUrl) {
        this.movieId = movieId;
        this.trailerUrl = trailerUrl;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }
}
