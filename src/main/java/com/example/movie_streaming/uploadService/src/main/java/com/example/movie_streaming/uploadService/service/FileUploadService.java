package com.example.movie_streaming.uploadService.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileUploadService {

    // Lấy bucket name từ file application.properties
    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;

    // Tải file lên Google Cloud Storage
    public String uploadFileToCloud(MultipartFile file) throws IOException {
        // Khởi tạo dịch vụ Google Cloud Storage
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Tạo metadata cho file
        String fileName = file.getOriginalFilename();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        // Tạo stream cho file
        InputStream mediaContent = file.getInputStream();

        // Tải file lên Google Cloud Storage
        Blob blob = storage.create(blobInfo, mediaContent);

        // Lấy URL của file đã upload
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;
        return fileUrl;
    }
}
