package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorId implements Serializable {
    private Long movieId;
    private Integer actorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieActorId)) return false;
        MovieActorId that = (MovieActorId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(actorId, that.actorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, actorId);
    }
}
