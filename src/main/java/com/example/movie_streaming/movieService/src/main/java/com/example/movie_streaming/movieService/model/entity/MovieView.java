package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_views")
public class MovieView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "viewed_at")
    private java.sql.Timestamp viewedAt;
}