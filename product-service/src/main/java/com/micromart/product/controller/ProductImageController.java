package com.micromart.product.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.product.domain.Product;
import com.micromart.product.service.ProductService;
import com.micromart.product.service.S3StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

/**
 * Product Image Controller - REST API for Product Image Management.
 * <p>
 * Handles image upload/download operations via S3.
 * <p>
 * Learning Points:
 * - MultipartFile for file uploads
 * - Pre-signed URLs for secure image access
 * - Separation of concerns (separate controller for images)
 */
@RestController
@RequestMapping("/products/{productId}/image")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Images", description = "Product image management endpoints")
public class ProductImageController {

    private final ProductService productService;
    private final S3StorageService s3StorageService;

    /**
     * Upload product image.
     * <p>
     * Note: Uses consumes = MULTIPART_FORM_DATA for file upload.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload image", description = "Upload a product image (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Uploading image for product {}, file: {}, size: {}",
                productId, file.getOriginalFilename(), file.getSize());

        // Verify product exists
        Product product = productService.getById(productId);

        // Delete old image if exists
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                s3StorageService.deleteFile(product.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", e.getMessage());
            }
        }

        // Upload new image
        String key = s3StorageService.uploadFile(file, "product-" + productId);

        // Update product with new image URL
        product.setImageUrl(key);
        // Note: In real implementation, you'd use ProductService.updateImage(id, key)

        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "key", key,
                        "url", s3StorageService.getPublicUrl(key)
                ),
                "Image uploaded successfully"
        ));
    }

    /**
     * Get product image URL.
     * Returns a pre-signed URL that's valid for 1 hour.
     */
    @GetMapping
    @Operation(summary = "Get image URL", description = "Get a pre-signed URL for product image")
    public ResponseEntity<ApiResponse<Map<String, String>>> getImageUrl(@PathVariable Long productId) {
        Product product = productService.getById(productId);

        if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("url", ""),
                    "No image available for this product"
            ));
        }

        String presignedUrl = s3StorageService.generatePresignedUrl(
                product.getImageUrl(),
                Duration.ofHours(1)
        );

        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "key", product.getImageUrl(),
                        "url", presignedUrl,
                        "expiresIn", "1 hour"
                )
        ));
    }

    /**
     * Delete product image.
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete image", description = "Delete a product image (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long productId) {
        log.info("Deleting image for product {}", productId);

        Product product = productService.getById(productId);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            s3StorageService.deleteFile(product.getImageUrl());
            product.setImageUrl(null);
            // Note: In real implementation, you'd use ProductService.deleteImage(id)
        }

        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }
}
