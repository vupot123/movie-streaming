package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity
public class MovieView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long movieId;
    private Long userId;
    private java.sql.Timestamp viewedAt;

    public MovieView() {
    }

    public MovieView(Long id, Long movieId, Long userId, Timestamp viewedAt) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
        this.viewedAt = viewedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public java.sql.Timestamp getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(java.sql.Timestamp viewedAt) {
        this.viewedAt = viewedAt;
    }
}
