package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.model.entity.Movie;

import java.util.HashMap;
import java.util.Map;

public class KafkaPayloadBuilder {

    public static Map<String, Object> buildCreatePayload(CreateMovieRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", request.getTitle());
        payload.put("type", request.getType());
        payload.put("year", request.getYear());
        payload.put("duration", request.getDuration());
        payload.put("intro", request.getIntro());
        payload.put("ageRating", request.getAgeRating());
        payload.put("views", request.getViews() != null ? request.getViews() : 0L);
        payload.put("actorIds", request.getActorIds());
        payload.put("genreIds", request.getGenreIds());
        payload.put("countryIds", request.getCountryIds());
        payload.put("trailerUrls", request.getTrailerUrls());
        payload.put("smallBanner", request.getSmallBanner());
        payload.put("largeBanner", request.getLargeBanner());
        return payload;
    }

    public static Map<String, Object> buildUpdatePayload(Movie movie, UpdateMovieRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", movie.getTitle());
        payload.put("type", movie.getType().toString());
        payload.put("year", movie.getYear());
        payload.put("duration", movie.getDuration());
        payload.put("intro", movie.getIntro());
        payload.put("ageRating", movie.getAgeRating());
        payload.put("views", movie.getViews());
        payload.put("actorIds", request.getActorIds());
        payload.put("genreIds", request.getGenreIds());
        payload.put("countryIds", request.getCountryIds());
        payload.put("trailerUrls", request.getTrailerUrls());
        payload.put("smallBanner", request.getSmallBanner());
        payload.put("largeBanner", request.getLargeBanner());
        return payload;
    }

    public static Map<String, Object> buildViewPayload(Long id, Long views) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("views", views);
        return payload;
    }
}