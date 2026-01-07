# MicroMart - E-Commerce Microservices Platform

## Project Overview
**Project Name:** MicroMart
**Base Package:** `com.micromart`
**Repository:** https://github.com/Poojithvsc/micromart-project

A comprehensive e-commerce platform built with microservices architecture, demonstrating Martin Fowler's PEAA patterns, Spring Boot best practices, and cloud-native technologies.

---

## Architecture Diagram

```
                                    ┌─────────────────┐
                                    │   GitHub Actions │
                                    │   (CI/CD)        │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │    Terraform     │
                                    │  (AWS Infra)     │
                                    └────────┬────────┘
                                             │
┌──────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud                                     │
│  ┌─────────────┐    ┌─────────────────────────────────────────────────┐   │
│  │    S3       │    │                 EC2 Instance                     │   │
│  │  (Images)   │    │  ┌───────────────────────────────────────────┐  │   │
│  └─────────────┘    │  │              Docker Compose                │  │   │
│                     │  │                                            │  │   │
│                     │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │   │
│                     │  │  │ Eureka   │  │   API    │  │  Kafka   │ │  │   │
│                     │  │  │ Server   │  │ Gateway  │  │          │ │  │   │
│                     │  │  └──────────┘  └────┬─────┘  └──────────┘ │  │   │
│                     │  │                     │                      │  │   │
│                     │  │  ┌──────────────────┼──────────────────┐  │  │   │
│                     │  │  │                  │                   │  │  │   │
│                     │  │  ▼                  ▼                   ▼  │  │   │
│                     │  │ ┌────────┐    ┌──────────┐    ┌─────────┐ │  │   │
│                     │  │ │ User   │    │ Product  │    │  Order  │ │  │   │
│                     │  │ │Service │    │ Service  │    │ Service │ │  │   │
│                     │  │ └────────┘    └──────────┘    └─────────┘ │  │   │
│                     │  └───────────────────────────────────────────┘  │   │
│                     └─────────────────────────────────────────────────┘   │
│                                             │                              │
│                                    ┌────────▼────────┐                    │
│                                    │   RDS PostgreSQL │                    │
│                                    │   (3 databases)  │                    │
│                                    └─────────────────┘                    │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Microservices (3 Core + 2 Infrastructure)

### 1. Infrastructure Services
| Service | Port | Purpose |
|---------|------|---------|
| eureka-server | 8761 | Service Discovery & Registry |
| api-gateway | 8080 | Routing, Auth, Rate Limiting |

### 2. Core Business Services
| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| user-service | 8081 | user_db | Authentication, User Management |
| product-service | 8082 | product_db | Catalog, Inventory, S3 Images |
| order-service | 8083 | order_db | Orders, Payments, Kafka Events |

---

## Martin Fowler's PEAA Patterns Implementation

| Pattern | Implementation Location |
|---------|------------------------|
| **Domain Model** | Rich entities in `domain/` packages with behavior |
| **Data Mapper** | JPA/Hibernate with `@Entity` annotations |
| **Repository** | Spring Data JPA `@Repository` interfaces |
| **Service Layer** | `@Service` classes with business logic |
| **Unit of Work** | `@Transactional` for transaction management |
| **Identity Map** | JPA EntityManager first-level cache |
| **Lazy Load** | `@EntityGraph`, `FetchType.LAZY` |
| **Value Object** | `@Embeddable` (Money, Address, Email) |
| **DTO** | Request/Response objects in `dto/` package |
| **Remote Facade** | `@RestController` as REST facade |
| **Gateway** | Spring Cloud Gateway for API routing |
| **Registry** | Eureka Server for service discovery |
| **Plugin** | `@Conditional`, `@Profile` for env-specific beans |
| **Separated Interface** | Interface-based design for all services |
| **Money Pattern** | Custom Money class with currency support |
| **Specification** | JPA Specifications for dynamic queries |

---

## Spring Boot Concepts Mapping

### Per Layer (Following Fowler's Layers)

```
┌─────────────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER                                                   │
│ @RestController, @GetMapping, @PostMapping, @RequestBody, @Valid    │
│ @ExceptionHandler, @ControllerAdvice, ResponseEntity                │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│ APPLICATION/SERVICE LAYER                                            │
│ @Service, @Transactional, @Async, @EventListener                    │
│ @TransactionalEventListener, ApplicationEvent                       │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER                                                         │
│ @Entity, @Embeddable, @ManyToOne, @OneToMany, @Embedded             │
│ Rich domain objects with business logic                              │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE/DATA SOURCE LAYER                                     │
│ @Repository, JpaRepository, @Query, Specification, @EntityGraph     │
│ Kafka producers/consumers, S3 client, external integrations         │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
micromart/
├── infrastructure/
│   ├── terraform/
│   │   ├── main.tf                 # Main Terraform config
│   │   ├── variables.tf            # Input variables
│   │   ├── outputs.tf              # Output values
│   │   ├── vpc.tf                  # VPC configuration
│   │   ├── ec2.tf                  # EC2 instance
│   │   ├── rds.tf                  # RDS PostgreSQL
│   │   ├── s3.tf                   # S3 bucket for images
│   │   └── security-groups.tf      # Security groups
│   └── docker/
│       └── docker-compose.yml      # Local development
│
├── .github/
│   └── workflows/
│       ├── ci.yml                  # Build & Test
│       └── cd.yml                  # Deploy to AWS
│
├── eureka-server/                  # Service Discovery
│   ├── src/main/java/.../
│   │   └── EurekaServerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── Dockerfile
│
├── api-gateway/                    # API Gateway
│   ├── src/main/java/.../
│   │   ├── ApiGatewayApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java       # JWT validation
│   │   │   ├── GatewayConfig.java         # Route config
│   │   │   └── RateLimitConfig.java       # Rate limiting
│   │   └── filter/
│   │       └── AuthenticationFilter.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── Dockerfile
│
├── user-service/                   # User & Auth Service
│   ├── src/main/java/.../
│   │   ├── UserServiceApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java        # @RestController
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── AuthService.java           # @Service interface
│   │   │   ├── AuthServiceImpl.java       # Implementation
│   │   │   ├── UserService.java
│   │   │   └── UserServiceImpl.java
│   │   ├── repository/
│   │   │   └── UserRepository.java        # @Repository
│   │   ├── domain/
│   │   │   ├── User.java                  # @Entity
│   │   │   ├── Role.java                  # @Entity
│   │   │   └── valueobject/
│   │   │       └── Email.java             # @Embeddable
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── SecurityConfig.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java # @ControllerAdvice
│   │   │   └── UserNotFoundException.java
│   │   └── config/
│   │       └── AppConfig.java             # @Configuration
│   ├── src/test/java/.../
│   │   ├── unit/
│   │   ├── integration/
│   │   └── architecture/
│   └── Dockerfile
│
├── product-service/                # Product & Inventory
│   ├── src/main/java/.../
│   │   ├── ProductServiceApplication.java
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   └── CategoryController.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── InventoryService.java
│   │   │   └── S3StorageService.java      # S3 integration
│   │   ├── repository/
│   │   │   ├── ProductRepository.java
│   │   │   └── specification/
│   │   │       └── ProductSpecification.java  # Specification pattern
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── Inventory.java
│   │   │   └── valueobject/
│   │   │       └── Money.java             # Money pattern
│   │   ├── kafka/
│   │   │   └── InventoryEventConsumer.java # Kafka consumer
│   │   ├── event/
│   │   │   ├── InventoryUpdatedEvent.java  # ApplicationEvent
│   │   │   └── InventoryEventListener.java # @EventListener
│   │   └── config/
│   │       ├── KafkaConfig.java
│   │       └── S3Config.java
│   └── Dockerfile
│
├── order-service/                  # Order Processing
│   ├── src/main/java/.../
│   │   ├── OrderServiceApplication.java
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── service/
│   │   │   ├── OrderService.java
│   │   │   └── PaymentService.java
│   │   ├── repository/
│   │   │   └── OrderRepository.java
│   │   ├── domain/
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderStatus.java
│   │   │   └── valueobject/
│   │   │       └── Address.java           # @Embeddable
│   │   ├── kafka/
│   │   │   ├── OrderEventProducer.java    # Kafka producer
│   │   │   └── event/
│   │   │       └── OrderCreatedEvent.java
│   │   ├── client/
│   │   │   ├── ProductClient.java         # @FeignClient
│   │   │   └── UserClient.java
│   │   └── saga/
│   │       └── OrderSagaOrchestrator.java # Saga pattern
│   └── Dockerfile
│
├── common/                         # Shared library
│   ├── src/main/java/.../
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── event/
│   │   └── util/
│   └── pom.xml
│
└── pom.xml                         # Parent POM
```

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| Build | Maven (multi-module) |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Messaging | Apache Kafka |
| Security | Spring Security + JWT |
| Cloud | AWS (EC2, RDS, S3) |
| IaC | Terraform |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Testing | JUnit 5, Mockito, Testcontainers |
| Documentation | OpenAPI/Swagger |
| Monitoring | Spring Actuator + Prometheus |

---

## Kafka Event Flow

```
┌──────────────┐     order.created      ┌─────────────────┐
│ Order        │ ────────────────────▶  │ Product Service │
│ Service      │                        │ (Inventory)     │
└──────────────┘                        └────────┬────────┘
       │                                         │
       │ order.payment.completed                 │ inventory.reserved
       ▼                                         ▼
┌──────────────┐                        ┌─────────────────┐
│ Notification │ ◀──────────────────────│ Order Service   │
│ (async)      │    inventory.updated   │ (update status) │
└──────────────┘                        └─────────────────┘
```

**Kafka Topics:**
- `order-events` - Order lifecycle events
- `inventory-events` - Stock updates and reservations
- `payment-events` - Payment processing events

---

## AWS Infrastructure (Terraform)

### Resources to Create:
1. **VPC** - Custom VPC with public/private subnets
2. **Security Groups** - For EC2, RDS, and internal communication
3. **EC2 Instance** - t2.medium for running Docker containers
4. **RDS PostgreSQL** - db.t3.micro for databases
5. **S3 Bucket** - For product images
6. **IAM Roles** - For EC2 to access S3

### Terraform Modules:
```
terraform/
├── main.tf           # Provider config, module calls
├── vpc.tf            # VPC, subnets, internet gateway
├── security-groups.tf # SG rules
├── ec2.tf            # EC2 instance with user_data
├── rds.tf            # RDS PostgreSQL instance
├── s3.tf             # S3 bucket for images
├── iam.tf            # IAM roles and policies
├── variables.tf      # Input variables
├── outputs.tf        # Outputs (IPs, endpoints)
└── terraform.tfvars  # Variable values (gitignored)
```

---

## Implementation Phases

### Phase 1: Project Foundation
- [ ] Create Maven multi-module project structure
- [ ] Set up parent POM with dependency management
- [ ] Create common shared module
- [ ] Configure Docker Compose for local development

### Phase 2: Infrastructure Services
- [ ] Implement Eureka Server
- [ ] Implement API Gateway with basic routing

### Phase 3: User Service
- [ ] Domain model (User, Role entities)
- [ ] Repository layer with Spring Data JPA
- [ ] Service layer with interface-based design
- [ ] REST controllers with validation
- [ ] JWT authentication
- [ ] Unit and integration tests

### Phase 4: Product Service
- [ ] Domain model (Product, Category, Inventory)
- [ ] Money value object pattern
- [ ] JPA Specifications for dynamic queries
- [ ] S3 integration for images
- [ ] Kafka consumer for inventory events
- [ ] Application events with @EventListener

### Phase 5: Order Service
- [ ] Domain model (Order, OrderItem)
- [ ] Address value object
- [ ] Feign clients for inter-service communication
- [ ] Kafka producer for order events
- [ ] Saga pattern for order orchestration
- [ ] Circuit breaker with Resilience4j

### Phase 6: Security & Cross-Cutting
- [ ] JWT validation in API Gateway
- [ ] @PreAuthorize annotations
- [ ] Global exception handling
- [ ] Actuator endpoints and health checks

### Phase 7: Testing
- [ ] Unit tests with Mockito
- [ ] Integration tests with Testcontainers
- [ ] Architecture tests with ArchUnit

### Phase 8: Terraform & AWS
- [ ] VPC and networking
- [ ] EC2 instance configuration
- [ ] RDS PostgreSQL setup
- [ ] S3 bucket for images
- [ ] Security groups and IAM

### Phase 9: CI/CD
- [ ] GitHub Actions for build and test
- [ ] Docker image building
- [ ] Deployment pipeline to AWS

---

## Key Learning Outcomes

After completing this project, you will understand:

1. **Microservices Architecture** - Service decomposition, communication patterns
2. **PEAA Patterns** - Domain Model, Repository, Service Layer, Value Objects
3. **Spring Boot Mastery** - All core annotations and concepts
4. **Event-Driven Architecture** - Kafka for async communication
5. **API Gateway Pattern** - Routing, security, rate limiting
6. **Database Design** - JPA relationships, transactions
7. **Cloud Deployment** - AWS EC2, RDS, S3 with Terraform
8. **CI/CD Pipeline** - Automated testing and deployment
9. **Testing Strategies** - Unit, integration, architecture tests
10. **Security** - JWT, OAuth2, Spring Security

---

## Git Repository & Branching Strategy

**Repository:** https://github.com/Poojithvsc/micromart-project

### Branch Structure (GitFlow)

```
main (production-ready code)
  │
  └── dev (integration branch for features)
        │
        ├── feature/user-service
        ├── feature/product-service
        ├── feature/order-service
        ├── feature/api-gateway
        ├── feature/eureka-server
        ├── feature/terraform-infrastructure
        └── feature/github-actions
```

### Branch Naming Conventions
| Branch Type | Pattern | Example |
|-------------|---------|---------|
| Main | `main` | `main` |
| Development | `dev` | `dev` |
| Feature | `feature/<name>` | `feature/user-service` |
| Bugfix | `bugfix/<name>` | `bugfix/login-validation` |
| Hotfix | `hotfix/<name>` | `hotfix/security-patch` |
| Release | `release/<version>` | `release/1.0.0` |

### Branch Protection Rules (Recommended)
- **main**: Require PR, require status checks, no direct push
- **dev**: Require PR from feature branches

### Commit Message Convention
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example:**
```
feat(user-service): add JWT authentication

- Implement JwtTokenProvider
- Add SecurityConfig with filter chain
- Create login and register endpoints

Closes #12
```

---

## Files to Create (Estimated)

- **Java files**: ~80-100 files across services
- **Configuration**: ~15-20 YAML/properties files
- **Terraform**: ~10 .tf files
- **Docker**: ~6 Dockerfiles + docker-compose
- **GitHub Actions**: 2 workflow files
- **Tests**: ~40-50 test files
