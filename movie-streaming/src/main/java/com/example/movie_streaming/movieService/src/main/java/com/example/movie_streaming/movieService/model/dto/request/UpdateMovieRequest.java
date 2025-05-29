package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMovieRequest {
    private String title; // Tiêu đề phim (tùy chọn)
    private String type; // Loại phim: "LE" hoặc "BO" (tùy chọn)
    private Integer year; // Năm phát hành (tùy chọn)
    private Integer duration; // Thời lượng (phút) (tùy chọn)
    private String intro; // Giới thiệu phim (tùy chọn)
    private String ageRating; // Độ tuổi: PG-13, R, v.v. (tùy chọn)
    private Long views; // Lượt xem (tùy chọn)
    private List<Long> actorIds; // Danh sách ID diễn viên (tùy chọn)
    private List<Integer> genreIds; // Danh sách ID thể loại (tùy chọn)
    private List<Integer> countryIds; // Danh sách ID quốc gia (tùy chọn)
    private List<String> trailerUrls; // Danh sách URL trailer (tùy chọn)
    private String smallBanner; // URL banner nhỏ (tùy chọn)
    private String largeBanner; // URL banner lớn (tùy chọn)
}
