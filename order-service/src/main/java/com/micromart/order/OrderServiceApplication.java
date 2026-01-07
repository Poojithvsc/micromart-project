package com.micromart.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Order Service Application - Order Processing with Kafka and Feign.
 * <p>
 * PEAA Patterns demonstrated:
 * - Value Object: Address embedded value object
 * - Domain Events: Kafka events for order lifecycle
 * - Gateway: Feign clients for inter-service communication
 * <p>
 * Spring Cloud Concepts:
 * - @EnableFeignClients: Enables declarative REST clients
 * - Service Discovery: Integrates with Eureka
 * <p>
 * Access the service at: http://localhost:8083
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
