package com.example.movie_streaming.streamService.controller;

import com.example.movie_streaming.streamService.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileId") String fileId) {
        try {
            String fileUrl = fileUploadService.uploadFileToCloud(file, fileId);
            return ResponseEntity.ok("Tệp đã được tải lên thành công. URL tệp: " + fileUrl);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi tải tệp: " + e.getMessage());
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileId") String fileId, @RequestParam("fileName") String fileName) {
        try {
            fileUploadService.deleteFile(fileId, fileName);
            return ResponseEntity.ok("Tệp đã được xóa thành công cho fileId: " + fileId);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi xóa tệp: " + e.getMessage());
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> getAllFileUrls() {
        try {
            List<String> fileUrls = fileUploadService.getAllFileUrls();
            return ResponseEntity.ok(fileUrls);
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
