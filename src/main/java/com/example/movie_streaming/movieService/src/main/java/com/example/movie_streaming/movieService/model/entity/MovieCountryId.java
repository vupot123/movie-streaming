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
public class MovieCountryId implements Serializable {
    private Long movieId;
    private Integer countryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCountryId that = (MovieCountryId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(countryId, that.countryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, countryId);
    }
}