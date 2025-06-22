package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.response.ActorResponse;
import com.example.movie_streaming.movieService.model.entity.Actor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActorMapper {

    public ActorResponse toResponse(Actor actor) {
        return new ActorResponse(
                actor.getId(),
                actor.getName(),
                actor.getGender() != null ? actor.getGender().name() : null,
                actor.getDob(),
                actor.getAvatarUrl(),
                actor.getBio(),
                null // hoặc List.of() nếu bạn muốn trả về mảng rỗng
        );
    }

}
