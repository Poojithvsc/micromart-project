package com.micromart.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application - Service Discovery and Registry.
 * <p>
 * PEAA Pattern: Registry
 * A well-known object that other objects can use to find common objects and services.
 * <p>
 * Spring Cloud Annotation: @EnableEurekaServer
 * Enables this application to act as a Eureka Server for service registration
 * and discovery. All microservices will register with this server and can
 * discover other services through it.
 * <p>
 * Key Features:
 * - Service Registration: Microservices register on startup
 * - Service Discovery: Services find each other by name
 * - Health Monitoring: Tracks service health status
 * - Self-Preservation Mode: Prevents mass deregistration during network issues
 * <p>
 * Access the Eureka Dashboard at: http://localhost:8761
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/registry.html">Registry Pattern</a>
 * @see org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
