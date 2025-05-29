package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_trailers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieTrailer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;

    private String url;
}