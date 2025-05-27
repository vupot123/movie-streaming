package com.example.movie_streaming.streamService.controller;

import com.example.movie_streaming.streamService.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // Endpoint POST to upload a file
    @PostMapping("/movie")
    public ResponseEntity<String> uploadMovie(@RequestParam("File") MultipartFile file, @RequestParam("movieId") Long movieId) {
        try {
            String fileUrl = fileUploadService.uploadFileToCloud(file, movieId);
            return ResponseEntity.ok("File uploaded successfully. File URL: " + fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Endpoint GET to retrieve all files for a movieId
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Map<String, Object>>> getFileInfo(@PathVariable Long movieId) {
        try {
            List<Map<String, Object>> fileInfo = fileUploadService.getFileInfo(movieId);
            return ResponseEntity.ok(fileInfo);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(List.of(Map.of("error", "No files found: " + e.getMessage())));
        }
    }

    // Endpoint GET to retrieve all files
    @GetMapping("/movies")
    public ResponseEntity<List<Map<String, Object>>> getAllFiles() {
        try {
            List<Map<String, Object>> files = fileUploadService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", "Failed to retrieve files: " + e.getMessage())));
        }
    }

    @DeleteMapping("/movie/{movieId}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long movieId, @RequestParam("fileName") String fileName) {
        try {
            fileUploadService.deleteFile(movieId, fileName);
            return ResponseEntity.ok("File deleted successfully for movieId: " + movieId + ", fileName: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }
}