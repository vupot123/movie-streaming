package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final KafkaProducerService kafkaProducerService;
    private final Storage storage;
    private final ObjectMapper objectMapper;
    private final SingleMovieStreamRepository singleMovieStreamRepository;

    @Autowired
    public FileUploadService(KafkaProducerService kafkaProducerService, Storage storage,
                             ObjectMapper objectMapper, SingleMovieStreamRepository singleMovieStreamRepository) {
        this.kafkaProducerService = kafkaProducerService;
        this.storage = storage;
        this.objectMapper = objectMapper;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
    }

    public String uploadFileToCloud(MultipartFile file) throws IOException {
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
        sendKafkaMessage("UPLOAD", null, uniqueFileName, fileUrl, file.getContentType());

        return fileUrl;
    }

    /**
     * Yêu cầu xóa một file khỏi Google Cloud Storage và database thông qua Kafka message dựa trên fileId.
     * @param fileId ID của file trong database
     * @throws Exception Nếu không tìm thấy file hoặc xảy ra lỗi
     */
    public void deleteFile(Long fileId) throws Exception {
        // Kiểm tra file trong database dựa trên fileId
        Optional<SingleMovieStream> streamOpt = singleMovieStreamRepository.findById(fileId);
        if (!streamOpt.isPresent()) {
            throw new Exception("Không tìm thấy tệp với fileId: " + fileId);
        }

        SingleMovieStream stream = streamOpt.get();
        String fileName = stream.getFileName();
        String fileUrl = stream.getFileUrl();

        // Gửi Kafka message để consumer xử lý xóa cả GCS và database
        sendKafkaMessage("DELETE", fileId, fileName, fileUrl, null);

        logger.info("Sent DELETE request for fileId: {} to Kafka", fileId);
    }

    /**
     * Lấy thông tin file dựa trên fileId (thay vì movieId)
     * @param fileId ID của file (có thể null)
     * @return Map chứa thông tin file hoặc trạng thái
     */
    public Map<String, Object> getFileInfo(Long fileId) {
        Map<String, Object> fileInfo = new HashMap<>();
        try {
            if (fileId != null) {
                Optional<SingleMovieStream> stream = singleMovieStreamRepository.findById(fileId);
                if (stream.isPresent()) {
                    fileInfo.put("file", stream.get());
                } else {
                    fileInfo.put("status", "NOT_FOUND");
                    logger.warn("No stream found for fileId: {}", fileId);
                }
            } else {
                fileInfo.put("status", "NO_FILE_ID_PROVIDED");
            }
            return fileInfo;
        } catch (Exception e) {
            logger.error("Error retrieving file info for fileId {}: {}", fileId, e.getMessage());
            throw new RuntimeException("Failed to retrieve file info: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả thông tin file từ database
     * @return Map chứa danh sách file hoặc trạng thái
     */
    public Map<String, Object> getAllFiles() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SingleMovieStream> streams = singleMovieStreamRepository.findAllByOrderByIdAsc();
            if (streams.isEmpty()) {
                response.put("status", "NO_FILES_FOUND");
                logger.warn("No streams found in database");
            } else {
                response.put("files", streams);
            }
            return response;
        } catch (Exception e) {
            logger.error("Error retrieving all files: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve all files: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tất cả các SingleMovieStream từ database
     */
    public List<SingleMovieStream> getAllFileStreams() {
        try {
            List<SingleMovieStream> streams = singleMovieStreamRepository.findAllByOrderByIdAsc();
            if (streams.isEmpty()) {
                logger.warn("No streams found in database");
            }
            return streams;
        } catch (Exception e) {
            logger.error("Error retrieving all file streams: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve file streams: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm danh sách SingleMovieStream dựa trên từ khóa (fileName hoặc fileUrl)
     * @param search Từ khóa tìm kiếm
     * @return Danh sách SingleMovieStream khớp với từ khóa
     */
    public List<SingleMovieStream> searchFileStreams(String search) {
        try {
            if (search == null || search.trim().isEmpty()) {
                return getAllFileStreams(); // Trả về tất cả nếu search rỗng
            }

            // Tìm kiếm dựa trên fileName hoặc fileUrl (sử dụng LIKE để tìm kiếm không chính xác)
            List<SingleMovieStream> streams = singleMovieStreamRepository.findByFileNameContainingIgnoreCaseOrFileUrlContainingIgnoreCase(search);
            if (streams.isEmpty()) {
                logger.warn("No streams found for search term: {}", search);
            }
            return streams;
        } catch (Exception e) {
            logger.error("Error searching file streams for term {}: {}", search, e.getMessage());
            throw new RuntimeException("Failed to search file streams: " + e.getMessage(), e);
        }
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

    private void sendKafkaMessage(String action, Long fileId, String fileName, String fileUrl, String contentType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fileId", fileId != null ? fileId : ""); // Sử dụng fileId thay movieId
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