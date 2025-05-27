package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.kafka.KafkaMessage;
import com.example.movie_streaming.streamService.kafka.KafkaProducerService;
import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    public String uploadFileToCloud(MultipartFile file, Long movieId) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = makeUniqueFileName(originalFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType())
                .build();

        try (InputStream mediaContent = file.getInputStream()) {
            storage.create(blobInfo, mediaContent);
        }

        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + uniqueFileName;
        sendKafkaMessage("UPLOAD", movieId, uniqueFileName, fileUrl, file.getContentType());
        return fileUrl;
    }

    public void deleteFile(Long movieId, String fileName) {
        Optional<SingleMovieStream> stream = singleMovieStreamRepository.findByMovieIdAndFileName(movieId, fileName);
        if (!stream.isPresent()) {
            throw new RuntimeException("File not found for movieId: " + movieId + " and fileName: " + fileName);
        }

        SingleMovieStream singleMovieStream = stream.get();
        try {
            storage.delete(bucketName, singleMovieStream.getFileName());
            sendKafkaMessage("DELETE", movieId, singleMovieStream.getFileName(), singleMovieStream.getFileUrl(), null);
        } catch (Exception e) {
            logger.warn("Failed to delete file {} from GCS for movieId: {}. Error: {}", fileName, movieId, e.getMessage());
            throw new RuntimeException("Failed to delete file from GCS: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getFileInfo(Long movieId) {
        List<SingleMovieStream> streams = singleMovieStreamRepository.findAllByMovieId(movieId);
        if (streams.isEmpty()) {
            throw new RuntimeException("No files found for movieId: " + movieId);
        }

        List<Map<String, Object>> files = new ArrayList<>();
        for (SingleMovieStream stream : streams) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("movieId", stream.getMovieId());
            fileInfo.put("fileName", stream.getFileName());
            fileInfo.put("fileUrl", stream.getFileUrl());

            Blob blob = storage.get(bucketName, stream.getFileName());
            fileInfo.put("contentType", blob != null ? blob.getContentType() : "Unknown");
            fileInfo.put("size", blob != null ? blob.getSize() : 0);
            fileInfo.put("created", blob != null ? blob.getCreateTime() : 0);

            files.add(fileInfo);

            sendKafkaMessage("GET", movieId, stream.getFileName(), stream.getFileUrl(), blob != null ? blob.getContentType() : null);
        }

        return files;
    }

    public List<Map<String, Object>> getAllFiles() {
        List<Map<String, Object>> files = new ArrayList<>();
        List<SingleMovieStream> streams = singleMovieStreamRepository.findAll();

        for (SingleMovieStream stream : streams) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", stream.getFileName());
            fileInfo.put("fileUrl", stream.getFileUrl());
            fileInfo.put("movieId", stream.getMovieId());

            Blob blob = storage.get(bucketName, stream.getFileName());
            fileInfo.put("contentType", blob != null ? blob.getContentType() : "Unknown");
            fileInfo.put("size", blob != null ? blob.getSize() : 0);
            fileInfo.put("created", blob != null ? blob.getCreateTime() : 0);

            files.add(fileInfo);
        }

        sendKafkaMessage("GET_ALL", null, null, null, null);
        return files;
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

    private void sendKafkaMessage(String action, Long movieId, String fileName, String fileUrl, String contentType) {
        Map<String, Object> payload = Map.of(
                "movieId", movieId != null ? movieId : "",
                "fileName", fileName != null ? fileName : "",
                "fileUrl", fileUrl != null ? fileUrl : "",
                "contentType", contentType != null ? contentType : ""
        );
        // Pass null for entityId to match constructor
        KafkaMessage kafkaMessage = new KafkaMessage("file-upload", action, null, payload);
        String messageJson = convertToJson(kafkaMessage);
        kafkaProducerService.sendMessage("file-uploaded-topic", messageJson);
    }

    private String convertToJson(KafkaMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }
}