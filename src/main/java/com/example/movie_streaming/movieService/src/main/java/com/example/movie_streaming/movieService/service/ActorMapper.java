package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.request.ActorRequest;
import com.example.movie_streaming.movieService.model.dto.response.ActorResponse;
import com.example.movie_streaming.movieService.model.entity.Actor;
import com.example.movie_streaming.movieService.model.entity.Gender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component // Đảm bảo annotation này có mặt
public class ActorMapper {

    public ActorResponse toResponse(Actor actor) {
        List<Long> movieIds = actor.getMovieActors() != null
                ? actor.getMovieActors().stream()
                .map(ma -> ma.getMovie().getId())
                .distinct()
                .toList()
                : List.of();

        return new ActorResponse(
                actor.getId(),
                actor.getName(),
                actor.getGender() != null ? actor.getGender().name() : null,
                actor.getDob(),
                actor.getAvatarUrl(),
                actor.getBio(),
                movieIds
        );
    }

    public Actor updateActorFromRequest(Actor actor, ActorRequest request) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            actor.setName(request.getName());
        }
        if (request.getDob() != null) {
            actor.setDob(request.getDob());
        }
        if (request.getBio() != null) {
            actor.setBio(request.getBio());
        }
        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                actor.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Bỏ qua nếu gender không hợp lệ
            }
        }
        if (request.getAvatarUrl() != null) {
            actor.setAvatarUrl(request.getAvatarUrl());
        }
        return actor;
    }
}