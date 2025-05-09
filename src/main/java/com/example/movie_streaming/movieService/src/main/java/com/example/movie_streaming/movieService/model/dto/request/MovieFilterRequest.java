package com.example.movie_streaming.movieService.model.dto.request;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieFilterRequest {
    private List<String> genres;          // tên thể loại
    private List<String> countries;       // tên quốc gia
    private List<Integer> years;          // danh sách năm phát hành
    private String quality;               // chất lượng video (nếu có)
    private String type;                  // "le" hoặc "bo"
    private String status;                // trạng thái (nếu có dùng)
    private String excludeStatus;         // loại trừ theo status
    private List<String> versions;        // bản phụ đề, lồng tiếng...
    private String rating;                // giới hạn độ tuổi (PG-13, R...)
    private List<String> networks;        // đài sản xuất (nếu có dùng)
    private List<String> productions;     // nhà sản xuất (nếu có dùng)
    private String sort;                  // sắp xếp: "release_date", "views", "rating", ...
    private Integer page = 0;             // trang hiện tại (bắt đầu từ 0)
    private Integer size = 10;            // số phần tử/trang
    private String keyword;               // tìm kiếm theo tiêu đề
}
