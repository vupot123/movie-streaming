package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;

    @Column(name = "small_banner")
    private String smallBanner;

    @Column(name = "large_banner")
    private String largeBanner;
}