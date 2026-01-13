# MicroMart - Glossary of Terms

A comprehensive glossary of all technical terms used in this project.

---

## A

### API (Application Programming Interface)
A set of rules that allows different software applications to communicate with each other. In MicroMart, REST APIs allow the frontend to talk to backend services.

### API Gateway
A server that acts as the single entry point for all client requests. It routes requests to appropriate microservices, handles authentication, rate limiting, and more. MicroMart uses Spring Cloud Gateway on port 8080.

### Authentication
The process of verifying who a user is. In MicroMart, users authenticate by providing username/password and receiving a JWT token.

### Authorization
The process of verifying what a user is allowed to do. In MicroMart, roles (USER, ADMIN) determine access to endpoints.

---

## B

### Bean
In Spring, a bean is an object that is managed by the Spring container. Beans are created, configured, and destroyed by Spring.

### BCrypt
A password hashing algorithm used to securely store passwords. MicroMart uses BCrypt to hash user passwords before storing them.

### Bounded Context
A DDD (Domain-Driven Design) concept where each microservice handles a specific business domain. User Service handles users, Order Service handles orders.

---

## C

### Circuit Breaker
A design pattern that prevents cascading failures. If a service is down, the circuit breaker "opens" and fails fast instead of waiting. MicroMart uses Resilience4j.

### Controller
A Spring class that handles HTTP requests. Marked with `@RestController`, it maps URLs to methods.

### CORS (Cross-Origin Resource Sharing)
A security feature that controls which domains can access your API. API Gateway configures CORS for the entire system.

### CRUD
Create, Read, Update, Delete - the four basic operations on data. Each entity has CRUD operations exposed via REST APIs.

---

## D

### DAO (Data Access Object)
An older term for Repository. A class that handles database operations.

### Dependency Injection (DI)
A technique where objects receive their dependencies from external sources rather than creating them. Spring handles DI automatically.

### Docker
A platform for running applications in containers. Each microservice can be packaged as a Docker image.

### Domain
The subject area or business logic your application deals with. In MicroMart, the domain includes users, products, orders.

### DTO (Data Transfer Object)
An object that carries data between processes. `CreateUserRequest` and `UserResponse` are DTOs - they don't contain business logic.

---

## E

### Embeddable
A JPA annotation for classes that are embedded into entities. Value objects like `Email`, `Money`, `Address` are embeddable.

### Entity
A JPA class that maps to a database table. `User`, `Product`, `Order` are entities with `@Entity` annotation.

### Eureka
Netflix's service discovery server. Services register with Eureka and discover each other through it. Runs on port 8761.

### Event
A notification that something happened. `UserRegisteredEvent` is published when a user registers. Events enable loose coupling.

### Event-Driven Architecture
A design pattern where services communicate by publishing and subscribing to events, rather than direct calls.

---

## F

### Feign
A declarative HTTP client for calling other services. Order Service uses `@FeignClient` to call Product Service.

### Filter
In Spring Security, a component that intercepts and processes requests. `JwtAuthenticationFilter` validates JWT tokens.

---

## G

### Gateway
See API Gateway.

---

## H

### Health Check
An endpoint that reports if a service is healthy. Available at `/actuator/health`.

### Hibernate
The JPA implementation used by Spring. It translates Java objects to SQL.

---

## I

### Idempotent
An operation that produces the same result regardless of how many times it's executed. GET requests should be idempotent.

### Inventory
Tracking of product stock. The `Inventory` entity tracks quantity, reserved quantity, and reorder levels.

---

## J

### JPA (Java Persistence API)
A specification for ORM (Object-Relational Mapping) in Java. Allows mapping Java objects to database tables.

### JSON (JavaScript Object Notation)
A lightweight data format used for API requests and responses.

### JWT (JSON Web Token)
A compact, URL-safe token format for representing claims. MicroMart uses JWT for authentication.

---

## K

### Kafka
A distributed message queue for event streaming. Services publish and consume events through Kafka topics.

---

## L

### Load Balancing
Distributing requests across multiple service instances. Eureka enables client-side load balancing.

### Lombok
A Java library that reduces boilerplate code. `@Data`, `@Builder`, `@Slf4j` are Lombok annotations.

---

## M

### MapStruct
A code generator that creates type-safe mappers between DTOs and entities at compile time.

### Microservices
An architectural style where an application is composed of small, independent services.

### Monolith
A traditional architecture where all functionality is in a single application. Opposite of microservices.

---

## N

### N+1 Problem
A database performance issue where fetching N entities results in N+1 queries. Solved with `@EntityGraph` or JOIN FETCH.

---

## O

### ORM (Object-Relational Mapping)
A technique for converting between Java objects and relational database records. JPA/Hibernate provides ORM.

### Optimistic Locking
A concurrency control method using version numbers. `@Version` in `Inventory` prevents lost updates.

---

## P

### POJO (Plain Old Java Object)
A simple Java object without special restrictions. Entities and DTOs are POJOs.

### PostgreSQL
An open-source relational database. Each MicroMart service has its own PostgreSQL database.

---

## R

### Repository
A Spring Data interface for database operations. Extends `JpaRepository<Entity, ID>`.

### REST (Representational State Transfer)
An architectural style for APIs. Uses HTTP methods (GET, POST, PUT, DELETE) and returns JSON.

### Role
A user's permission level. MicroMart has USER, ADMIN, and MODERATOR roles.

---

## S

### S3 (Simple Storage Service)
Amazon's cloud storage service. MicroMart uses S3 to store product images.

### Saga Pattern
A way to manage distributed transactions across microservices. Each service performs its action and publishes an event.

### Service Discovery
The mechanism by which services find each other. Eureka provides service discovery.

### Service Layer
The layer containing business logic. Marked with `@Service`.

### Spring Boot
A framework that simplifies Spring application development with auto-configuration.

### Spring Cloud
A set of tools for building cloud-native applications (Gateway, Eureka, Config, etc.).

### Spring Security
A framework for authentication and authorization in Spring applications.

---

## T

### Token
A string that represents authentication credentials. JWT tokens are used in MicroMart.

### Transaction
A unit of work that either completes entirely or not at all. `@Transactional` marks transaction boundaries.

---

## U

### UUID (Universally Unique Identifier)
A 128-bit identifier. Order numbers like `ORD-uuid` use UUIDs for uniqueness.

---

## V

### Validation
Checking that input data meets requirements. Jakarta Validation (`@NotBlank`, `@Email`) handles this.

### Value Object
An immutable object defined by its attributes, not its identity. `Email`, `Money`, `Address` are value objects.

---

## W

### Webhook
An HTTP callback triggered by an event. Could be used for notifications.

---

## Y

### YAML
A human-readable configuration format. Spring Boot uses `application.yml` for configuration.

---

## Numbers

### 8080, 8081, 8082, 8083, 8761
Ports used by MicroMart services:
- 8080: API Gateway
- 8081: User Service
- 8082: Product Service
- 8083: Order Service
- 8761: Eureka Server

---

*This glossary covers the main terms. For deeper understanding, see the LLD document.*
