package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMovieRequest {
    private String title; // Tiêu đề phim (bắt buộc)
    private String type; // Loại phim: "LE" hoặc "BO" (bắt buộc)
    private Integer year; // Năm phát hành
    private Integer duration; // Thời lượng (phút)
    private String intro; // Giới thiệu phim
    private String ageRating; // Độ tuổi: PG-13, R, v.v.
    private Long views; // Lượt xem (mặc định 0 nếu không cung cấp)
    private List<Long> actorIds; // Danh sách ID diễn viên
    private List<Integer> genreIds; // Danh sách ID thể loại
    private List<Integer> countryIds; // Danh sách ID quốc gia
    private List<String> trailerUrls; // Danh sách URL trailer
    private String smallBanner; // URL banner nhỏ
    private String largeBanner; // URL banner lớn
}
