package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSeasonRequest {
    private String name;
    private Integer seasonNumber;
    private List<CreateEpisodeRequest> episodes;
}
