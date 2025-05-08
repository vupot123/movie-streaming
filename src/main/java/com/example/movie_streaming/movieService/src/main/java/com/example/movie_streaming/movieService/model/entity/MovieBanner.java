package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_banners")
public class MovieBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "small_banner", columnDefinition = "text", nullable = false)
    private String smallBanner;

    @Column(name = "large_banner", columnDefinition = "text", nullable = false)
    private String largeBanner;
}