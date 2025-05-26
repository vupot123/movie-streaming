package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class FileUploadService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final KafkaProducerService kafkaProducerService;
    private final Storage storage;
    private final ObjectMapper objectMapper;

    public FileUploadService(KafkaProducerService kafkaProducerService, Storage storage, ObjectMapper objectMapper) {
        this.kafkaProducerService = kafkaProducerService;
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    public String uploadFileToCloud(MultipartFile file, Long movieId) throws IOException {

        String originalFileName = file.getOriginalFilename();

        // Tạo tên file duy nhất bằng cách kiểm tra trùng trong GCS
        String uniqueFileName = makeUniqueFileName(originalFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType())
                .build();

        // Tải file lên Google Cloud Storage
        try (InputStream mediaContent = file.getInputStream()) {
            storage.create(blobInfo, mediaContent);
        }

        // Tạo URL công khai của file
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + uniqueFileName;

        // Gửi Kafka message với thông tin file
        sendKafkaMessage("UPLOAD", movieId, uniqueFileName, fileUrl, file.getContentType());

        return fileUrl;
    }

    public void deleteFile(Long movieId) {
        sendKafkaMessage("DELETE", movieId, null, null, null);
    }

    private String makeUniqueFileName(String fileName) {
        String uniqueFileName = fileName;
        int suffix = 1;

        // Tách tên file và phần mở rộng
        String namePart = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";

        // Kiểm tra trùng tên trong GCS bucket
        while (storage.get(bucketName, uniqueFileName) != null) {
            uniqueFileName = namePart + "_" + suffix + extension;
            suffix++;
        }

        return uniqueFileName;
    }

    private void sendKafkaMessage(String action, Long movieId, String fileName, String fileUrl, String contentType) {
        Map<String, Object> payload = Map.of(
                "movieId", movieId,
                "fileName", fileName != null ? fileName : "",
                "fileUrl", fileUrl != null ? fileUrl : "",
                "contentType", contentType != null ? contentType : ""
        );
        KafkaMessage kafkaMessage = new KafkaMessage("file-upload", action, null, payload);
        String messageJson = convertToJson(kafkaMessage);
        kafkaProducerService.sendMessage("file-upload-topic", messageJson);
    }

    private String convertToJson(KafkaMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}