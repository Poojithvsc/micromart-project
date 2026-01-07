package com.micromart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application - Entry Point for All Client Requests.
 * <p>
 * PEAA Pattern: Gateway
 * An object that encapsulates access to an external system or resource.
 * In this case, it encapsulates access to all microservices.
 * <p>
 * Spring Cloud Annotation: @EnableDiscoveryClient
 * Enables service discovery through Eureka for dynamic routing.
 * <p>
 * Key Features:
 * - Request Routing: Routes requests to appropriate microservices
 * - Authentication: Validates JWT tokens before forwarding requests
 * - Rate Limiting: Prevents abuse with configurable rate limits
 * - Circuit Breaker: Prevents cascade failures with Resilience4j
 * - Load Balancing: Distributes requests across service instances
 * - Request Logging: Logs all incoming requests for debugging
 * <p>
 * Access the gateway at: http://localhost:8080
 * API Documentation: http://localhost:8080/swagger-ui.html
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/gateway.html">Gateway Pattern</a>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
