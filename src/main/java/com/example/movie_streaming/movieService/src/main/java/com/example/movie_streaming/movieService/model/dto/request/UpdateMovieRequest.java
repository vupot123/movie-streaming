package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMovieRequest {
    private Long id;
    private String title;
    private String type;
    private Integer year;
    private Integer duration;
    private String intro;
    private String ageRating;
    private Long views;
}
