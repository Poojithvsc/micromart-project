package com.micromart.product.actuator;

import com.micromart.product.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Custom Health Indicator for S3 Connection.
 * <p>
 * Checks if S3 bucket is accessible.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Health health() {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .build();

            s3Client.headBucket(request);

            return Health.up()
                    .withDetail("bucket", s3Properties.getBucket())
                    .withDetail("region", s3Properties.getRegion())
                    .withDetail("status", "accessible")
                    .build();

        } catch (NoSuchBucketException e) {
            log.warn("S3 bucket does not exist: {}", s3Properties.getBucket());
            return Health.down()
                    .withDetail("bucket", s3Properties.getBucket())
                    .withDetail("status", "bucket not found")
                    .withDetail("error", e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("S3 health check failed", e);
            return Health.down()
                    .withDetail("bucket", s3Properties.getBucket())
                    .withDetail("status", "error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
