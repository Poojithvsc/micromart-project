package com.micromart.product.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3 Configuration.
 * <p>
 * Spring Concepts Demonstrated:
 * - @Value: Inject individual property values
 * - @ConfigurationProperties: Type-safe property binding (via S3Properties)
 * - @Profile: Conditional bean creation based on active profile
 * - @Bean: Programmatic bean definition
 * <p>
 * Learning Points:
 * - @Value("${prop:default}") syntax for defaults
 * - Different S3 clients for different environments
 * - S3Presigner for generating pre-signed URLs
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
@RequiredArgsConstructor
@Slf4j
public class S3Config {

    private final S3Properties s3Properties;

    /**
     * Example of @Value injection.
     * Useful for simple values, @ConfigurationProperties better for complex config.
     */
    @Value("${aws.access-key-id:#{null}}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:#{null}}")
    private String secretAccessKey;

    /**
     * S3 Client for production.
     * Uses default AWS credential chain (env vars, IAM role, etc.)
     */
    @Bean
    @Profile("!local")
    public S3Client s3Client() {
        log.info("Creating S3 client for region: {}", s3Properties.getRegion());

        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * S3 Client for local development.
     * Can use LocalStack or MinIO for local S3 emulation.
     * <p>
     * @Profile("local") means this bean is only created when
     * spring.profiles.active=local is set.
     */
    @Bean
    @Profile("local")
    public S3Client localS3Client() {
        log.info("Creating local S3 client with endpoint: {}", s3Properties.getEndpoint());

        var builder = S3Client.builder()
                .region(Region.of(s3Properties.getRegion()));

        // Use custom endpoint for LocalStack/MinIO
        if (s3Properties.getEndpoint() != null) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }

        // Use static credentials for local
        if (accessKeyId != null && secretAccessKey != null) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    )
            );
        }

        return builder.build();
    }

    /**
     * S3 Presigner for generating pre-signed URLs.
     * Pre-signed URLs allow temporary direct access to S3 objects.
     */
    @Bean
    @Profile("!local")
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("local")
    public S3Presigner localS3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()));

        if (s3Properties.getEndpoint() != null) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }

        if (accessKeyId != null && secretAccessKey != null) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    )
            );
        }

        return builder.build();
    }
}
