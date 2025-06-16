package com.example.movie_streaming.streamService.controller;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService; // Constructor injection thay cho @Autowired
    }

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            logger.debug("Yêu cầu upload file: {}", file.getOriginalFilename());
            if (file.isEmpty()) {
                logger.warn("File upload rỗng");
                return ResponseEntity.badRequest().body("File không được để trống");
            }
            String fileUrl = fileUploadService.uploadFileToCloud(file);
            logger.info("Tệp {} đã được tải lên thành công. URL: {}", file.getOriginalFilename(), fileUrl);
            return ResponseEntity.ok("Tệp đã được tải lên thành công. URL tệp: " + fileUrl);
        } catch (Exception e) {
            logger.error("Lỗi khi upload file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Lỗi khi tải tệp: " + e.getMessage());
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileId") Long fileId) {
        try {
            logger.debug("Yêu cầu xóa file với fileId: {}", fileId);
            fileUploadService.deleteFile(fileId);
            logger.info("Tệp với fileId {} đã được xóa thành công", fileId);
            return ResponseEntity.ok("Tệp đã được xóa thành công cho fileId: " + fileId);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa file với fileId {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Lỗi khi xóa tệp: " + e.getMessage());
        }
    }

    @GetMapping("/files")
    public ResponseEntity<Page<SingleMovieStream>> getAllFileUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.debug("Yêu cầu lấy tất cả file urls với page: {}, size: {}", page, size);
            Page<SingleMovieStream> streams = fileUploadService.getAllFileStreams(page, size);
            logger.info("Đã lấy được {} file streams", streams.getTotalElements());
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tất cả file urls: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/files/search")
    public ResponseEntity<Page<SingleMovieStream>> searchFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.debug("Yêu cầu tìm kiếm file với keyword: {}, type: {}, page: {}, size: {}", keyword, type, page, size);
            Page<SingleMovieStream> streams = fileUploadService.searchFileStreams(keyword, page, size);

            if (type != null && !type.trim().isEmpty()) {
                List<SingleMovieStream> filteredStreams = filterByFileType(streams.getContent(), type.trim().toLowerCase());
                streams = new PageImpl<>(filteredStreams, PageRequest.of(page, size), streams.getTotalElements());
                if (filteredStreams.isEmpty()) {
                    logger.info("Không tìm thấy stream nào cho type: {}", type);
                }
            }

            logger.info("Đã tìm thấy {} streams", streams.getTotalElements());
            return ResponseEntity.ok(streams);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

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
}