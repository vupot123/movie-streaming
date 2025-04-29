package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MovieBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long movieId;
    private String smallBanner;
    private String largeBanner;

    public MovieBanner() {
    }

    public MovieBanner(Long id, Long movieId, String smallBanner, String largeBanner) {
        this.id = id;
        this.movieId = movieId;
        this.smallBanner = smallBanner;
        this.largeBanner = largeBanner;
    }

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

    public String getSmallBanner() {
        return smallBanner;
    }

    public void setSmallBanner(String smallBanner) {
        this.smallBanner = smallBanner;
    }

    public String getLargeBanner() {
        return largeBanner;
    }

    public void setLargeBanner(String largeBanner) {
        this.largeBanner = largeBanner;
    }
}
