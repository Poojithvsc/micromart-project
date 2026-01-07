package com.micromart.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service Application - Authentication and User Management.
 * <p>
 * This service demonstrates multiple PEAA patterns:
 * - Domain Model: Rich User and Role entities
 * - Repository: Spring Data JPA repositories
 * - Service Layer: Business logic encapsulation
 * - Data Mapper: JPA/Hibernate ORM
 * - Value Object: Email as embedded value object
 * - Unit of Work: @Transactional
 * - DTO: Request/Response objects
 * - Remote Facade: REST controllers
 * <p>
 * Spring Boot Concepts:
 * - @EnableJpaAuditing: Enables automatic timestamp auditing
 * - @EnableDiscoveryClient: Registers with Eureka server
 * <p>
 * Access the service at: http://localhost:8081
 * API Documentation: http://localhost:8081/swagger-ui.html
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
