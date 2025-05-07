package com.example.movie_streaming.uploadService.controller;

import com.example.movie_streaming.uploadService.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/movie")
    public String uploadMovie(@RequestParam("File") MultipartFile file) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());
            return fileUploadService.uploadFileToCloud(file);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
