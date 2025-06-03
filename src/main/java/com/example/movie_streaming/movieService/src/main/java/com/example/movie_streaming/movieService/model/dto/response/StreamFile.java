package com.example.movie_streaming.movieService.model.dto.response;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StreamFile {
    private String fileName;
    private long size;
    private long created;
    private long movieId;
    private String fileUrl;
    private String contentType;
}

