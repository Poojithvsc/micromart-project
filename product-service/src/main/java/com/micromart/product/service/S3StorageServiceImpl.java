package com.micromart.product.service;

import com.micromart.product.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * S3 Storage Service Implementation.
 * <p>
 * Handles file uploads to AWS S3 for product images.
 * <p>
 * Learning Points:
 * - AWS SDK v2 for Java
 * - Pre-signed URLs for temporary access
 * - Content type validation
 * - Unique file naming to prevent overwrites
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageServiceImpl implements S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public String uploadFile(MultipartFile file, String fileName) {
        validateFile(file);

        String key = generateKey(fileName != null ? fileName : file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("Uploaded file to S3: bucket={}, key={}", s3Properties.getBucket(), key);
            return key;

        } catch (IOException e) {
            log.error("Failed to read file for S3 upload", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        } catch (S3Exception e) {
            log.error("S3 error during upload: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public String uploadFile(byte[] bytes, String fileName, String contentType) {
        validateContentType(contentType);
        validateFileSize(bytes.length);

        String key = generateKey(fileName);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

            log.info("Uploaded bytes to S3: bucket={}, key={}, size={}",
                    s3Properties.getBucket(), key, bytes.length);
            return key;

        } catch (S3Exception e) {
            log.error("S3 error during upload: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from S3: bucket={}, key={}", s3Properties.getBucket(), key);

        } catch (S3Exception e) {
            log.error("S3 error during delete: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("S3 delete failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getRequest)
                    .build();

            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.debug("Generated pre-signed URL for key={}, duration={}", key, duration);
            return url;

        } catch (S3Exception e) {
            log.error("Failed to generate pre-signed URL: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to generate pre-signed URL", e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        // Assumes bucket is public or fronted by CloudFront
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucket(),
                s3Properties.getRegion(),
                key);
    }

    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 error checking file existence: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to check file existence", e);
        }
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        validateContentType(file.getContentType());
        validateFileSize(file.getSize());
    }

    private void validateContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }

        boolean allowed = Arrays.asList(s3Properties.getAllowedContentTypes())
                .contains(contentType);

        if (!allowed) {
            throw new IllegalArgumentException(
                    String.format("Content type '%s' not allowed. Allowed types: %s",
                            contentType,
                            Arrays.toString(s3Properties.getAllowedContentTypes())));
        }
    }

    private void validateFileSize(long size) {
        if (size > s3Properties.getMaxFileSize()) {
            throw new IllegalArgumentException(
                    String.format("File size %d exceeds maximum allowed size %d",
                            size, s3Properties.getMaxFileSize()));
        }
    }

    /**
     * Generate unique S3 key with path prefix.
     * Format: products/yyyy-MM/uuid-originalname.ext
     */
    private String generateKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String safeName = sanitizeFilename(originalFilename);

        // Add date-based partitioning for better S3 performance
        String datePath = java.time.LocalDate.now().toString().substring(0, 7); // yyyy-MM

        return String.format("%s/%s/%s-%s",
                s3Properties.getPathPrefix(),
                datePath,
                uuid,
                safeName);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "file";
        }
        // Remove path separators and special characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
