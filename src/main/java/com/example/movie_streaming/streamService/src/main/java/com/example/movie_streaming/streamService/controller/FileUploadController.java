package com.example.movie_streaming.streamService.controller;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileUploadService.uploadFileToCloud(file);
            return ResponseEntity.ok("Tệp đã được tải lên thành công. URL tệp: " + fileUrl);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi tải tệp: " + e.getMessage());
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileId") Long fileId) {
        try {
            fileUploadService.deleteFile(fileId);
            return ResponseEntity.ok("Tệp đã được xóa thành công cho fileId: " + fileId);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi xóa tệp: " + e.getMessage());
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<SingleMovieStream>> getAllFileUrls() {
        try {
            List<SingleMovieStream> streams = fileUploadService.getAllFileStreams();
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping("/files/search")
    public ResponseEntity<List<SingleMovieStream>> searchFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type) {
        try {
            List<SingleMovieStream> streams = fileUploadService.searchFileStreams(keyword);

            // Lọc theo loại file nếu type được cung cấp
            if (type != null && !type.trim().isEmpty()) {
                streams = filterByFileType(streams, type.trim().toLowerCase());
                if (streams.isEmpty()) {
                    logger.info("No streams found for type: {}", type);
                }
            }

            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Lọc danh sách SingleMovieStream theo loại file (ảnh hoặc video).
     * @param streams Danh sách chưa lọc
     * @param type Loại file (images hoặc videos)
     * @return Danh sách đã lọc
     */
    private List<SingleMovieStream> filterByFileType(List<SingleMovieStream> streams, String type) {
        List<SingleMovieStream> filteredStreams = new ArrayList<>();
        for (SingleMovieStream stream : streams) {
            String fileName = stream.getFileName().toLowerCase();
            if ("images".equals(type)) {
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
                    filteredStreams.add(stream);
                }
            } else if ("videos".equals(type)) {
                if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov") || fileName.endsWith(".mkv")) {
                    filteredStreams.add(stream);
                }
            }
        }
        return filteredStreams;
    }

    // Thêm logger để debug (nếu cần)
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileUploadController.class);
}