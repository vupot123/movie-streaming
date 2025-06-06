package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileUploadService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final KafkaProducerService kafkaProducerService;
    private final Storage storage;
    private final ObjectMapper objectMapper;
    private final SingleMovieStreamRepository singleMovieStreamRepository;

    public FileUploadService(KafkaProducerService kafkaProducerService, Storage storage,
                             ObjectMapper objectMapper, SingleMovieStreamRepository singleMovieStreamRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.storage = storage;
        this.objectMapper = objectMapper;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
    }

    public String uploadFileToCloud(MultipartFile file, String fileId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp không được rỗng hoặc null");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Tên tệp không hợp lệ");
        }

        String uniqueFileName = makeUniqueFileName(originalFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        try (InputStream mediaContent = file.getInputStream()) {
            storage.create(blobInfo, mediaContent);
        }

        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + uniqueFileName;
        sendKafkaMessage("UPLOAD", fileId, uniqueFileName, fileUrl, file.getContentType());
        return fileUrl;
    }

    public void deleteFile(String fileId, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Tên tệp không hợp lệ để xóa");
        }
        boolean deleted = storage.delete(bucketName, fileName);
        if (!deleted) {
            throw new RuntimeException("Không thể xóa tệp: " + fileName);
        }
        sendKafkaMessage("DELETE", fileId, fileName, null, null);
    }

    public List<String> getAllFileUrls() {
        try {
            List<String> urls = singleMovieStreamRepository.findAll().stream()
                    .map(stream -> stream.getFileUrl())
                    .filter(url -> url != null && !url.isEmpty())
                    .collect(Collectors.toList());
            return urls.isEmpty() ? Collections.emptyList() : urls;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách URL: " + e.getMessage(), e);
        }
    }

    private String makeUniqueFileName(String fileName) {
        String uniqueFileName = fileName;
        int suffix = 1;
        String namePart = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";

        while (storage.get(bucketName, uniqueFileName) != null) {
            uniqueFileName = namePart + "_" + suffix + extension;
            suffix++;
        }
        return uniqueFileName;
    }

    private void sendKafkaMessage(String action, String fileId, String fileName, String fileUrl, String contentType) {
        Map<String, Object> payload = Map.of(
                "fileId", fileId != null ? fileId : "",
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
            throw new RuntimeException("Lỗi khi chuyển đổi sang JSON: " + e.getMessage(), e);
        }
    }
}
