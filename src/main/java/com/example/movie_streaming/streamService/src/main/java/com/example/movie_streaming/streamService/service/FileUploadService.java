package com.example.movie_streaming.streamService.service;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import com.example.movie_streaming.streamService.repository.SingleMovieStreamRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final Storage storage;
    private final SingleMovieStreamRepository singleMovieStreamRepository;

    @Autowired
    public FileUploadService(Storage storage, SingleMovieStreamRepository singleMovieStreamRepository) {
        this.storage = storage;
        this.singleMovieStreamRepository = singleMovieStreamRepository;
    }

    public String uploadFileToCloud(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("File is null or empty: {}", file != null ? file.getOriginalFilename() : "null");
            throw new IOException("File is null or empty");
        }

        String originalFileName = file.getOriginalFilename();
        logger.debug("Received file for upload: {}", originalFileName);

        // Tạo tên file duy nhất bằng cách kiểm tra trùng trong GCS
        String uniqueFileName = makeUniqueFileName(originalFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uniqueFileName)
                .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        // Tải file lên Google Cloud Storage
        try (InputStream mediaContent = file.getInputStream()) {
            logger.debug("Starting upload of file {} to GCS bucket {}", uniqueFileName, bucketName);
            storage.create(blobInfo, mediaContent);
            logger.debug("Upload completed for file {}", uniqueFileName);
        } catch (IOException e) {
            logger.error("Failed to upload file {} to GCS: {}", uniqueFileName, e.getMessage(), e);
            throw new IOException("Failed to upload file to GCS: " + e.getMessage(), e);
        }

        // Tạo URL công khai của file
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + uniqueFileName;

        // Lưu thông tin file vào database ngay lập tức, bao gồm dung lượng
        SingleMovieStream stream = new SingleMovieStream();
        stream.setFileName(uniqueFileName);
        stream.setFileUrl(fileUrl);
        stream.setFileSize(file.getSize()); // Lấy dung lượng file từ MultipartFile
        try {
            singleMovieStreamRepository.save(stream);
            logger.info("Uploaded file {} to GCS and saved to database. URL: {}, Size: {} bytes", uniqueFileName, fileUrl, file.getSize());
        } catch (Exception e) {
            logger.error("Failed to save file {} to database: {}", uniqueFileName, e.getMessage(), e);
            // Xóa file trên GCS nếu lưu database thất bại
            Blob blob = storage.get(bucketName, uniqueFileName);
            if (blob != null) {
                storage.delete(bucketName, uniqueFileName);
                logger.warn("Rolled back upload: Deleted file {} from GCS due to database error", uniqueFileName);
            }
            throw new IOException("Failed to save file to database: " + e.getMessage(), e);
        }

        return fileUrl;
    }

    public void deleteFile(Long fileId) throws Exception {
        // Kiểm tra file trong database dựa trên fileId
        Optional<SingleMovieStream> streamOpt = singleMovieStreamRepository.findById(fileId);
        if (!streamOpt.isPresent()) {
            throw new Exception("Không tìm thấy tệp với fileId: " + fileId);
        }

        SingleMovieStream stream = streamOpt.get();
        String fileName = stream.getFileName();
        String fileUrl = stream.getFileUrl();

        // Xóa file trên GCS
        Blob blob = storage.get(bucketName, fileName);
        if (blob != null) {
            storage.delete(bucketName, fileName);
            logger.info("Deleted file from GCS: {}", fileName);
        } else {
            logger.warn("File {} not found in GCS for fileId: {}", fileName, fileId);
        }

        // Xóa bản ghi trong database
        singleMovieStreamRepository.delete(stream);
        logger.info("Deleted SingleMovieStream from database for fileId: {}", fileId);
    }

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

    public Map<String, Object> getAllFiles(int page, int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.debug("Lấy tất cả file từ database với page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<SingleMovieStream> streamPage = singleMovieStreamRepository.findAllByOrderByIdAsc(pageable);
            if (streamPage.isEmpty()) {
                response.put("status", "NO_FILES_FOUND");
                logger.warn("Không tìm thấy stream nào trong database");
            } else {
                response.put("content", streamPage.getContent());
                response.put("totalElements", streamPage.getTotalElements());
                response.put("totalPages", streamPage.getTotalPages());
                response.put("currentPage", page);
            }
            return response;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tất cả file: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve all files: " + e.getMessage(), e);
        }
    }

    public Page<SingleMovieStream> getAllFileStreams(int page, int size) {
        try {
            logger.debug("Lấy tất cả file streams từ database với page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<SingleMovieStream> streamPage = singleMovieStreamRepository.findAllByOrderByIdAsc(pageable);
            if (streamPage.isEmpty()) {
                logger.warn("Không tìm thấy stream nào trong database");
            }
            return streamPage;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tất cả file streams: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve file streams: " + e.getMessage(), e);
        }
    }

    public Page<SingleMovieStream> searchFileStreams(String search, int page, int size) {
        try {
            logger.debug("Tìm kiếm file streams với từ khóa: {}, page: {}, size: {}", search, page, size);
            Pageable pageable = PageRequest.of(page, size);
            if (search == null || search.trim().isEmpty()) {
                return singleMovieStreamRepository.findAllByOrderByIdAsc(pageable);
            }
            Page<SingleMovieStream> streams = singleMovieStreamRepository.findByFileNameContainingIgnoreCaseOrFileUrlContainingIgnoreCase(search, pageable);
            if (streams.isEmpty()) {
                logger.warn("Không tìm thấy stream nào cho từ khóa: {}", search);
            }
            return streams;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm file streams với từ khóa {}: {}", search, e.getMessage());
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
}