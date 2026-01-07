# MicroMart - Progress Tracker

> **Last Updated:** January 2025 - Phase 4 Complete
> **Repository:** https://github.com/Poojithvsc/micromart-project

---

## 🚀 QUICK START FOR NEW SESSION

Copy and paste this to resume:

```
Read the progress tracker at docs/PROGRESS_TRACKER.md and continue building
the MicroMart project from where we left off.

Repository: https://github.com/Poojithvsc/micromart-project

⚠️ CRITICAL:
- Always work on `dev` branch (NEVER push directly to main)
- Run: git checkout dev && git pull origin dev

Current Status:
- Phases 1-4: ✅ COMPLETE
- Phase 5 (Terraform/AWS): 🔲 PENDING
- Phase 6 (CI/CD): 🔲 PENDING

Next task: Start Phase 5 (Terraform) or Phase 6 (CI/CD)
```

---

## 📊 Current Project Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Foundation | ✅ Complete | Multi-module Maven, Eureka, Gateway, service skeletons |
| Phase 2: Services | ✅ Complete | User, Product, Order services with full functionality |
| Phase 3: Kafka | ✅ Complete | Event-driven architecture with producers/consumers |
| Phase 4: Testing | ✅ Complete | Unit, Integration, Architecture tests |
| Phase 5: Terraform | 🔲 Pending | AWS infrastructure (EC2, RDS, S3) |
| Phase 6: CI/CD | 🔲 Pending | GitHub Actions pipelines |

---

## 🏗️ Service Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                        │
│                    JWT Validation, Rate Limiting                 │
└─────────────────────────┬───────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ User Service │  │   Product    │  │    Order     │
│    (8081)    │  │   Service    │  │   Service    │
│              │  │    (8082)    │  │    (8083)    │
│ - Auth/JWT   │  │ - Catalog    │  │ - Orders     │
│ - Users      │  │ - Inventory  │  │ - Workflow   │
│ - Events     │  │ - S3 Images  │  │ - Kafka Pub  │
└──────────────┘  │ - Kafka Sub  │  └──────────────┘
                  └──────────────┘
        │                 │                 │
        └─────────────────┴─────────────────┘
                          │
                ┌─────────┴─────────┐
                │  Eureka (8761)    │
                │ Service Discovery │
                └───────────────────┘
```

---

## 📁 Complete File Inventory

### Common Module (12 files)
```
common/src/main/java/com/micromart/common/
├── domain/
│   ├── AuditableEntity.java      # Base entity with audit fields
│   └── BaseEntity.java           # Base entity with ID
├── dto/
│   ├── ApiResponse.java          # Standard API response wrapper
│   ├── ErrorResponse.java        # Error response structure
│   └── PageResponse.java         # Pagination wrapper
├── event/
│   ├── BaseEvent.java            # Base event class
│   ├── EventType.java            # Event type enum
│   └── KafkaTopics.java          # Kafka topic constants
└── exception/
    ├── BaseException.java        # Base exception
    ├── BusinessException.java    # Business rule violations
    ├── DuplicateResourceException.java
    ├── ForbiddenException.java
    ├── GlobalExceptionHandler.java  # @ControllerAdvice
    ├── ResourceNotFoundException.java
    └── UnauthorizedException.java
```

### User Service (35 files)
```
user-service/src/main/java/com/micromart/user/
├── UserServiceApplication.java
├── actuator/
│   └── UserStatisticsHealthIndicator.java  # Custom health indicator
├── config/
│   ├── AppConfig.java            # @Qualifier, @Primary examples
│   ├── JwtConfig.java            # @ConfigurationProperties
│   └── SecurityConfig.java       # SecurityFilterChain
├── controller/
│   ├── AuthController.java       # Login, register, refresh
│   └── UserController.java       # User CRUD
├── domain/
│   ├── Role.java                 # @Entity
│   ├── User.java                 # @Entity with behavior
│   └── valueobject/
│       └── Email.java            # @Embeddable value object
├── dto/
│   ├── request/
│   │   ├── CreateUserRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── UpdateUserRequest.java
│   └── response/
│       ├── TokenResponse.java
│       └── UserResponse.java
├── event/
│   ├── UserAccountStatusEvent.java
│   ├── UserEvent.java            # Base ApplicationEvent
│   ├── UserEventListener.java    # @EventListener, @Async
│   ├── UserEventPublisher.java
│   ├── UserLoggedInEvent.java
│   └── UserRegisteredEvent.java
├── mapper/
│   └── UserMapper.java           # MapStruct mapper
├── repository/
│   └── UserRepository.java       # @EntityGraph, @Query
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
└── service/
    ├── AuthService.java          # Interface (Separated Interface pattern)
    ├── AuthServiceImpl.java
    ├── UserNotificationFacade.java
    ├── UserService.java
    ├── UserServiceImpl.java
    └── notification/
        ├── EmailNotificationService.java   # @Primary
        ├── MockNotificationService.java    # @Profile("test")
        ├── NotificationService.java        # Interface
        └── SmsNotificationService.java     # @Qualifier example
```

### Product Service (40+ files)
```
product-service/src/main/java/com/micromart/product/
├── ProductServiceApplication.java
├── actuator/
│   ├── InventoryHealthIndicator.java    # Custom health
│   └── S3HealthIndicator.java           # S3 connectivity check
├── config/
│   ├── AppConfig.java            # @EnableAsync
│   ├── KafkaConsumerConfig.java  # Kafka consumer setup
│   ├── S3Config.java             # @Profile, @Value examples
│   ├── S3Properties.java         # @ConfigurationProperties
│   └── SecurityConfig.java
├── controller/
│   ├── CategoryController.java   # CRUD for categories
│   ├── InventoryController.java  # Stock management
│   ├── ProductController.java    # Product CRUD + search
│   └── ProductImageController.java  # S3 image upload
├── domain/
│   ├── Category.java             # @Entity, @OneToMany
│   ├── Inventory.java            # @Entity, @Version (optimistic lock)
│   ├── Product.java              # @Entity, @Embedded Money
│   └── valueobject/
│       └── Money.java            # @Embeddable (Money pattern)
├── dto/
│   ├── request/
│   │   ├── CreateCategoryRequest.java
│   │   ├── CreateProductRequest.java
│   │   ├── InventoryUpdateRequest.java
│   │   ├── ProductSearchRequest.java
│   │   ├── StockReservationRequest.java
│   │   ├── UpdateCategoryRequest.java
│   │   └── UpdateProductRequest.java
│   └── response/
│       ├── CategoryResponse.java
│       ├── InventoryResponse.java
│       └── ProductResponse.java
├── event/
│   ├── InventoryEvent.java       # Base event
│   ├── InventoryEventListener.java  # @TransactionalEventListener
│   ├── InventoryEventPublisher.java
│   ├── LowStockEvent.java
│   ├── StockReleasedEvent.java
│   ├── StockReplenishedEvent.java
│   └── StockReservedEvent.java
├── kafka/
│   ├── OrderEventConsumer.java   # @KafkaListener
│   └── event/
│       └── OrderEvent.java       # DTO for Kafka messages
├── mapper/
│   ├── CategoryMapper.java
│   ├── InventoryMapper.java
│   └── ProductMapper.java
├── repository/
│   ├── CategoryRepository.java
│   ├── InventoryRepository.java  # Pessimistic locking
│   ├── ProductRepository.java
│   └── specification/
│       └── ProductSpecification.java  # JPA Specification pattern
└── service/
    ├── CategoryService.java
    ├── CategoryServiceImpl.java
    ├── InventoryService.java
    ├── InventoryServiceImpl.java
    ├── ProductService.java
    ├── ProductServiceImpl.java
    ├── S3StorageService.java     # Interface
    └── S3StorageServiceImpl.java # Pre-signed URLs
```

### Order Service (30+ files)
```
order-service/src/main/java/com/micromart/order/
├── OrderServiceApplication.java   # @EnableFeignClients
├── actuator/
│   ├── ExternalServicesHealthIndicator.java
│   └── OrdersHealthIndicator.java
├── client/
│   ├── InventoryClient.java      # @FeignClient
│   ├── InventoryClientFallback.java  # Circuit breaker fallback
│   ├── ProductClient.java        # @FeignClient
│   ├── ProductClientFallback.java
│   └── dto/
│       ├── ProductDto.java
│       └── StockReservationDto.java
├── config/
│   ├── FeignConfig.java          # Feign retry, logging
│   ├── KafkaConfig.java          # Kafka producer setup
│   └── SecurityConfig.java
├── controller/
│   └── OrderController.java      # Full order lifecycle
├── domain/
│   ├── Order.java                # @Entity with behavior
│   ├── OrderItem.java            # @Entity
│   ├── OrderStatus.java          # Enum
│   └── valueobject/
│       └── Address.java          # @Embeddable
├── dto/
│   ├── request/
│   │   ├── AddressRequest.java
│   │   ├── CreateOrderRequest.java
│   │   └── OrderItemRequest.java
│   └── response/
│       ├── AddressResponse.java
│       ├── OrderItemResponse.java
│       └── OrderResponse.java
├── kafka/
│   ├── OrderEventProducer.java   # Publishes to Kafka
│   └── event/
│       └── OrderEvent.java       # Event structure
├── mapper/
│   └── OrderMapper.java
├── repository/
│   └── OrderRepository.java      # @EntityGraph
└── service/
    ├── OrderService.java         # Interface
    └── OrderServiceImpl.java     # Coordinates Product + Inventory
```

### Infrastructure Services
```
eureka-server/src/main/java/com/micromart/eureka/
├── EurekaServerApplication.java
└── config/
    └── SecurityConfig.java

api-gateway/src/main/java/com/micromart/gateway/
├── ApiGatewayApplication.java
├── config/
│   └── JwtConfig.java
├── controller/
│   └── FallbackController.java   # Circuit breaker fallback
└── filter/
    ├── AuthenticationFilter.java  # JWT validation
    └── LoggingFilter.java
```

---

## ✅ Completed Requirements Checklist

### Technologies
- [x] Java 17
- [x] Spring Boot 3.2.x
- [x] REST APIs
- [x] Kafka (producers + consumers)
- [x] Docker + Docker Compose
- [x] PostgreSQL
- [x] S3 (AWS SDK integration)
- [x] Unit Tests (JUnit 5, Mockito, AssertJ)
- [x] Integration Tests (Testcontainers)
- [x] Architecture Tests (ArchUnit)
- [ ] Terraform (Phase 5)
- [ ] AWS EC2/RDS (Phase 5)
- [ ] GitHub Actions (Phase 6)

### Spring Boot Concepts
- [x] @Controller, @Service, @Repository, @Component
- [x] @Entity, @Embeddable
- [x] JpaRepository, @Query, @EntityGraph, @Transactional
- [x] Specification pattern
- [x] @ManyToOne, @OneToMany
- [x] Constructor injection, @Qualifier, @Primary, @Profile
- [x] @GetMapping, @PostMapping, @RequestBody, @Valid
- [x] @ExceptionHandler, @ControllerAdvice
- [x] application.yml, @Value, @ConfigurationProperties
- [x] SecurityFilterChain, @PreAuthorize, JWT
- [x] @EnableDiscoveryClient, @FeignClient, Circuit Breaker
- [x] ApplicationEvent, @EventListener, @TransactionalEventListener, @Async
- [x] Actuator health checks, custom HealthIndicator

### PEAA Patterns
- [x] Domain Model (rich entities with behavior)
- [x] Data Mapper (JPA/Hibernate)
- [x] Repository (Spring Data JPA)
- [x] Service Layer
- [x] Unit of Work (@Transactional)
- [x] Value Object (Email, Money, Address)
- [x] DTO pattern
- [x] Remote Facade (REST controllers)
- [x] Gateway (API Gateway, Feign)
- [x] Registry (Eureka)
- [x] Specification (JPA Specifications)
- [x] Money Pattern
- [x] Optimistic Locking (@Version)
- [x] Pessimistic Locking (SELECT FOR UPDATE)

---

## 🔧 Configuration Summary

### Service Ports
| Service | Port | Database |
|---------|------|----------|
| Eureka Server | 8761 | - |
| API Gateway | 8080 | - |
| User Service | 8081 | user_db |
| Product Service | 8082 | product_db |
| Order Service | 8083 | order_db |

### Kafka Topics
| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| order-events | order-service | product-service | Order lifecycle events |
| inventory-events | product-service | - | Stock change notifications |

### Key Environment Variables
```bash
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres
KAFKA_SERVERS=localhost:9092
EUREKA_URL=http://localhost:8761/eureka/
AWS_ACCESS_KEY_ID=your-key
AWS_SECRET_ACCESS_KEY=your-secret
S3_BUCKET=micromart-products
AWS_REGION=us-east-1
```

---

## 🔄 Git Workflow

```
feature/xyz → dev → Pull Request → main
```

| Rule | Action |
|------|--------|
| **Working branch** | Always `dev` |
| **Before starting** | `git checkout dev && git pull origin dev` |
| **Push to** | `dev` branch only |
| **Merge to main** | Create Pull Request |

**⚠️ NEVER push directly to main!**

---

## 📝 What's Next (Pending Phases)

### Phase 4: Testing ✅ COMPLETE
- [x] Unit tests with JUnit 5 + Mockito
- [x] Integration tests with Testcontainers
- [x] Architecture tests with ArchUnit
- [x] Controller tests with MockMvc
- [x] Test configurations (application-test.yml)

**Test Files Created:**
```
user-service/src/test/
├── java/com/micromart/user/
│   ├── architecture/ArchitectureTest.java       # ArchUnit rules
│   ├── controller/UserControllerTest.java       # MockMvc tests
│   ├── domain/UserTest.java                     # Entity tests
│   ├── domain/valueobject/EmailTest.java        # Value object tests
│   ├── repository/UserRepositoryIntegrationTest.java  # Testcontainers
│   └── service/UserServiceImplTest.java         # Service unit tests
└── resources/application-test.yml               # Test configuration

product-service/src/test/
├── java/com/micromart/product/
│   ├── domain/InventoryTest.java                # Entity tests
│   ├── domain/valueobject/MoneyTest.java        # Money pattern tests
│   └── service/InventoryServiceImplTest.java    # Service tests
└── resources/application-test.yml               # Test configuration

order-service/src/test/
├── java/com/micromart/order/
│   ├── domain/OrderTest.java                    # Order entity tests
│   └── service/OrderServiceImplTest.java        # Service tests
└── resources/application-test.yml               # Test configuration
```

**Run Tests:**
```bash
# Run all unit tests
mvn test

# Run unit + integration tests
mvn verify

# Run with coverage report (generates target/site/jacoco/index.html)
mvn verify -Pcoverage

# Run specific service tests
mvn test -pl user-service
```

### Phase 5: Terraform & AWS
- [ ] VPC with public/private subnets
- [ ] EC2 instance for Docker
- [ ] RDS PostgreSQL (3 databases)
- [ ] S3 bucket for images
- [ ] Security groups
- [ ] IAM roles for EC2 → S3

### Phase 6: CI/CD
- [ ] GitHub Actions build workflow
- [ ] Test automation
- [ ] Docker image publishing
- [ ] Deployment pipeline

---

## 🐛 Known Issues / Notes

1. **S3 Health Indicator**: Will fail if S3 bucket doesn't exist (expected in local dev)
2. **Kafka**: Requires Kafka broker running for full functionality
3. **Security**: Services trust API Gateway for auth; add defense-in-depth for production

---

## 📚 Additional Learning Topics Added

| Topic | Location | Why Important |
|-------|----------|---------------|
| Optimistic Locking | `Inventory.java` | Concurrent update safety |
| Pessimistic Locking | `InventoryRepository` | Critical sections |
| Pre-signed URLs | `S3StorageService` | Secure temporary access |
| Manual Kafka Ack | `OrderEventConsumer` | At-least-once delivery |
| Circuit Breaker | Feign fallbacks | Fault tolerance |
| Event Sourcing (lite) | Kafka events | Audit trail |

### Testing Concepts (Phase 4)
| Concept | Example File | Description |
|---------|--------------|-------------|
| JUnit 5 @Nested | `UserServiceImplTest` | Organize related tests in nested classes |
| Mockito @Mock/@InjectMocks | `OrderServiceImplTest` | Mock dependencies for isolation |
| AssertJ Fluent Assertions | All test files | Readable assertion chains |
| @ParameterizedTest | `EmailTest`, `MoneyTest` | Test multiple inputs with single test |
| Testcontainers | `UserRepositoryIntegrationTest` | Real PostgreSQL in Docker |
| @WebMvcTest + MockMvc | `UserControllerTest` | Test controllers without full context |
| @WithMockUser | `UserControllerTest` | Test Spring Security endpoints |
| ArchUnit | `ArchitectureTest` | Enforce architectural rules as tests |
| BDD Style (given/when/then) | All service tests | Readable test structure |

---

*Document auto-updated after each phase completion*
