package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMovieRequest {
    private Long id;

    private String title;
    private String subtitle;
    private String type;
    private Integer year;
    private Integer duration;
    private String intro;
    private String ageRating;
    private Long views;

    private List<String> genreNames;      // Tên thể loại (thay vì genreId)
    private String countryName;          // Tên quốc gia (thay vì countryId)

    private String smallBanner;
    private String largeBanner;

    private List<Long> actorIds;         // ID diễn viên đã có
    private List<ActorRequest> newActors; // Diễn viên mới

    private List<Long> collections;      // ID các bộ sưu tập
    private List<CreateSeasonRequest> seasons; // Cập nhật mùa và tập nếu có
}
