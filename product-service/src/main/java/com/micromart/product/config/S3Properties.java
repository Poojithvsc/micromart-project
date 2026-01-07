package com.micromart.product.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * S3 Configuration Properties.
 * <p>
 * Spring Concept: @ConfigurationProperties
 * Type-safe configuration binding from application.yml.
 * <p>
 * Learning Points:
 * - @ConfigurationProperties binds properties to a POJO
 * - @Validated enables JSR-303 validation on properties
 * - Prefix "aws.s3" maps to aws.s3.* in application.yml
 * - More type-safe than @Value for complex configurations
 */
@Data
@ConfigurationProperties(prefix = "aws.s3")
@Validated
public class S3Properties {

    /**
     * S3 bucket name for storing product images.
     */
    @NotBlank(message = "S3 bucket name is required")
    private String bucket;

    /**
     * AWS region for the S3 bucket.
     */
    @NotBlank(message = "AWS region is required")
    private String region;

    /**
     * Optional endpoint override (for LocalStack/MinIO testing).
     */
    private String endpoint;

    /**
     * Path prefix for all uploaded files.
     */
    private String pathPrefix = "products";

    /**
     * Maximum file size in bytes (default 5MB).
     */
    private long maxFileSize = 5 * 1024 * 1024;

    /**
     * Allowed content types for upload.
     */
    private String[] allowedContentTypes = {
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };
}
