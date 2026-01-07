package com.micromart.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Product Service Application - Catalog, Inventory, and S3 Storage.
 * <p>
 * PEAA Patterns demonstrated:
 * - Money Pattern: Money value object for prices
 * - Specification Pattern: Dynamic queries with JPA Specifications
 * - Domain Events: ApplicationEvent for inventory changes
 * - Repository Pattern: Spring Data JPA
 * <p>
 * Spring Boot Concepts:
 * - @EnableAsync: Enables async event processing
 * - @EnableJpaAuditing: Automatic timestamp auditing
 * <p>
 * Access the service at: http://localhost:8082
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableAsync
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
