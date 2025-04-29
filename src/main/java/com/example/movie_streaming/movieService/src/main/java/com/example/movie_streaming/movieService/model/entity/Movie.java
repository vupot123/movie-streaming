package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private int year;
    private int duration;
    private String intro;
    private String ageRating;
    private long views;

    public Movie() {
    }

    public Movie(Long id, String title, String type, int year, int duration, String intro, String ageRating, long views) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.year = year;
        this.duration = duration;
        this.intro = intro;
        this.ageRating = ageRating;
        this.views = views;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
