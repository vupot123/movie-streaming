package com.example.movie_streaming.streamService.controller;

import com.example.movie_streaming.streamService.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/movie")
    public String uploadMovie(@RequestParam("File") MultipartFile file, @RequestParam("movieId") Long movieId) {
        try {

            String fileUrl = fileUploadService.uploadFileToCloud(file, movieId);
            return "File uploaded successfully. File URL: " + fileUrl;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
