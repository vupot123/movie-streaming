package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final KafkaProducerService kafkaProducerService;
    private final Storage storage;
    private final ObjectMapper objectMapper;

    @Autowired
    public FileUploadService(KafkaProducerService kafkaProducerService, Storage storage,
                             ObjectMapper objectMapper) {
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
        } catch (IOException e) {
            logger.error("Failed to upload file {} to GCS: {}", uniqueFileName, e.getMessage());
            throw new IOException("Failed to upload file to GCS: " + e.getMessage(), e);
        }

        // Tạo URL công khai của file
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + uniqueFileName;

        // Gửi Kafka message với thông tin file để consumer xử lý lưu vào database
        sendKafkaMessage("UPLOAD", movieId, uniqueFileName, fileUrl, file.getContentType());

        return fileUrl;
    }

    public void deleteFile(Long movieId, String fileName) throws Exception {
        // Lấy thông tin file từ GCS để xác nhận trước khi gửi message
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null) {
            throw new Exception("Không tìm thấy tệp với fileName: " + fileName);
        }

        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;
        // Gửi Kafka message để consumer xử lý xóa từ database
        sendKafkaMessage("DELETE", movieId, fileName, fileUrl, null);

        // Xóa file trên GCS (để đảm bảo tính nhất quán)
        storage.delete(bucketName, fileName);
    }

    public Map<String, Object> getFileInfo(Long movieId) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("movieId", movieId);
        fileInfo.put("status", "REQUESTED");

        // Gửi Kafka message để consumer xử lý lấy thông tin từ database
        sendKafkaMessage("GET", movieId, null, null, null);

        return fileInfo;
    }

    public Map<String, Object> getAllFiles() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "REQUESTED");

        // Gửi Kafka message để consumer xử lý lấy tất cả thông tin từ database
        sendKafkaMessage("GET_ALL", null, null, null, null);

        return response;
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
        Map<String, Object> payload = new HashMap<>();
        payload.put("movieId", movieId != null ? movieId : "");
        payload.put("fileName", fileName != null ? fileName : "");
        payload.put("fileUrl", fileUrl != null ? fileUrl : "");
        payload.put("contentType", contentType != null ? contentType : "");
        payload.put("action", action);

        KafkaMessage kafkaMessage = new KafkaMessage("file-upload", action, null, payload);
        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("file-uploaded-topic", messageJson);
        } catch (Exception e) {
            logger.error("Failed to send Kafka message for action {}: {}", action, e.getMessage());
            throw new RuntimeException("Failed to send Kafka message: " + e.getMessage(), e);
        }
    }
}