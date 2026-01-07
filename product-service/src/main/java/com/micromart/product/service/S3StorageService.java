package com.micromart.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * S3 Storage Service Interface.
 * <p>
 * PEAA Pattern: Separated Interface
 * Allows different implementations (real S3, mock, LocalStack).
 */
public interface S3StorageService {

    /**
     * Upload a file to S3.
     *
     * @param file     the file to upload
     * @param fileName optional custom file name (null for auto-generated)
     * @return the S3 key (path) of the uploaded file
     */
    String uploadFile(MultipartFile file, String fileName);

    /**
     * Upload file bytes directly.
     *
     * @param bytes       file content
     * @param fileName    file name
     * @param contentType MIME type
     * @return the S3 key of the uploaded file
     */
    String uploadFile(byte[] bytes, String fileName, String contentType);

    /**
     * Delete a file from S3.
     *
     * @param key the S3 key of the file
     */
    void deleteFile(String key);

    /**
     * Generate a pre-signed URL for downloading a file.
     *
     * @param key      the S3 key
     * @param duration how long the URL should be valid
     * @return pre-signed URL string
     */
    String generatePresignedUrl(String key, Duration duration);

    /**
     * Generate a public URL for a file (assumes public bucket or CloudFront).
     *
     * @param key the S3 key
     * @return public URL string
     */
    String getPublicUrl(String key);

    /**
     * Check if a file exists in S3.
     *
     * @param key the S3 key
     * @return true if file exists
     */
    boolean fileExists(String key);
}
