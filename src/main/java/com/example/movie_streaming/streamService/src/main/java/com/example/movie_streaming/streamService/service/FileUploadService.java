package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class FileUploadService {

    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;

    private final KafkaProducerService kafkaProducerService;
    private final SingleMovieStreamRepository singleMovieStreamRepository;

    public FileUploadService(KafkaProducerService kafkaProducerService, SingleMovieStreamRepository singleMovieStreamRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
    }

    // Hàm tải file lên Google Cloud Storage và lưu thông tin vào database
    public String uploadFileToCloud(MultipartFile file, Long movieId) throws IOException {
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

        // URL công khai của file
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;

        // Lưu thông tin vào database
        saveFileDetailsToDatabase(movieId, fileName, fileUrl);

        // Gửi Kafka message về file upload
        sendKafkaMessage("UPLOAD", fileName, Map.of("contentType", file.getContentType()));

        // Trả về URL công khai của file
        return fileUrl;
    }

    // Hàm lưu thông tin file vào database
    private void saveFileDetailsToDatabase(Long movieId, String fileName, String fileUrl) {
        SingleMovieStream singleMovieStream = new SingleMovieStream(movieId, fileName, fileUrl);
        singleMovieStreamRepository.save(singleMovieStream);
    }

    // Hàm gửi thông báo tới Kafka khi có hành động upload
    private void sendKafkaMessage(String action, String fileName, Map<String, Object> payload) {
        KafkaMessage kafkaMessage = new KafkaMessage("file-upload", action, null, payload);
        String messageJson = convertToJson(kafkaMessage);
        kafkaProducerService.sendMessage("file-upload-topic", messageJson);
    }

    // Chuyển đổi Kafka message thành chuỗi JSON
    private String convertToJson(KafkaMessage message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
