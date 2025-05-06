package com.example.movie_streaming.movieService.model.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(columnDefinition = "TEXT")
    private String url;
}

