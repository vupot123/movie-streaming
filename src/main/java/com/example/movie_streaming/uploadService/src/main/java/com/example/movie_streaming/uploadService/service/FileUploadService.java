package com.example.movie_streaming.uploadService.service;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileUploadService {

    // Lấy tên bucket từ file application.properties
    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;

    // Hàm tải file lên Google Cloud Storage
    public String uploadFileToCloud(MultipartFile file) throws IOException {
        // Khởi tạo dịch vụ Google Cloud Storage
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Lấy tên file
        String fileName = file.getOriginalFilename();

        // Tạo metadata với Content-Type đúng (ví dụ video/mp4, image/jpeg, ...)
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        // Đọc nội dung file
        try (InputStream mediaContent = file.getInputStream()) {
            // Tải file lên Cloud Storage
            storage.create(blobInfo, mediaContent);
        }

        // Trả về URL công khai của file
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }
}
