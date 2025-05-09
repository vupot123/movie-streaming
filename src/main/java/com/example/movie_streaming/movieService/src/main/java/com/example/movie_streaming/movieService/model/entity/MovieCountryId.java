package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCountryId implements Serializable {
    private Long movieId;
    private Long countryId;
}
