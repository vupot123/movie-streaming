package com.example.movie_streaming.movieService.model.dto.request;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieFilterRequest {
    private List<String> genres; // Tên thể loại
    private List<String> countries; // Tên quốc gia
    private List<Integer> years; // Danh sách năm phát hành
    private String type; // "LE" hoặc "BO"
    private List<String> versions; // Bản phụ đề, lồng tiếng (dubbed, subtitled)
    private String rating; // Giới hạn độ tuổi (PG-13, R, v.v.)
    private String sort; // Sắp xếp: "release_date", "views", "title"
    private Integer page = 0; // Trang hiện tại (bắt đầu từ 0)
    private Integer size = 10; // Số phần tử mỗi trang
    private String keyword; // Từ khóa tìm kiếm theo tiêu đề
}
