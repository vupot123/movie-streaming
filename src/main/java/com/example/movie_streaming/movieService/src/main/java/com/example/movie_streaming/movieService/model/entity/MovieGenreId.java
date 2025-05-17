package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreId implements Serializable {
    private Long movieId;
    private Integer genreId;

    // 👇 BẮT BUỘC override equals() và hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieGenreId)) return false;
        MovieGenreId that = (MovieGenreId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(genreId, that.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, genreId);
    }
}

