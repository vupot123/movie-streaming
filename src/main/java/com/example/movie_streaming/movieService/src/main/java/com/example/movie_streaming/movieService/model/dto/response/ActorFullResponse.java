package com.example.movie_streaming.movieService.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorFullResponse {
    private Long id;
    private String name;
    private String gender;
    private LocalDate dob;
    private String avatarUrl;
    private String bio;
    private List<MovieResponse> movies;
}
