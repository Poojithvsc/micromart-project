# MicroMart - Progress Tracker

> **Last Updated:** January 2025 - All Phases Complete + Bug Fixes Applied
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
- Phases 1-6: ✅ COMPLETE
- Bug Fixes: ✅ ALL COMPILATION ERRORS RESOLVED
- All phases implemented!

The project compiles successfully and is feature-complete. Review and customize as needed.
```

---

## 📊 Current Project Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Foundation | ✅ Complete | Multi-module Maven, Eureka, Gateway, service skeletons |
| Phase 2: Services | ✅ Complete | User, Product, Order services with full functionality |
| Phase 3: Kafka | ✅ Complete | Event-driven architecture with producers/consumers |
| Phase 4: Testing | ✅ Complete | Unit, Integration, Architecture tests |
| Phase 5: Terraform | ✅ Complete | AWS infrastructure (EC2, RDS, S3) |
| Phase 6: CI/CD | ✅ Complete | GitHub Actions pipelines for CI/CD |

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
- [x] Terraform (Infrastructure as Code)
- [x] AWS VPC, EC2, RDS, S3, IAM
- [x] GitHub Actions CI/CD

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

### Phase 5: Terraform & AWS ✅ COMPLETE
- [x] VPC with public/private subnets
- [x] EC2 instance for Docker
- [x] RDS PostgreSQL with Secrets Manager
- [x] S3 bucket for images with lifecycle policies
- [x] Security groups (ALB, EC2, RDS, Kafka)
- [x] IAM roles for EC2 → S3, ECR, Secrets Manager, CloudWatch

**Terraform Files Created:**
```
terraform/
├── main.tf              # Provider configuration, locals
├── variables.tf         # All configurable variables with validation
├── outputs.tf           # Useful output values
├── vpc.tf               # VPC, subnets, IGW, NAT, route tables
├── security_groups.tf   # Security groups for all components
├── ec2.tf               # EC2 Docker host with CloudWatch
├── rds.tf               # RDS PostgreSQL with parameter groups
├── s3.tf                # S3 bucket with versioning, encryption
├── iam.tf               # IAM roles and policies
├── templates/
│   └── user_data.sh     # EC2 bootstrap script (Docker, Compose)
├── terraform.tfvars.example
├── .gitignore
└── README.md
```

**Deploy Commands:**
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
terraform init
terraform plan
terraform apply
```

### Phase 6: CI/CD ✅ COMPLETE
- [x] CI workflow (build, test, code quality)
- [x] CD workflow (deploy to AWS via SSM)
- [x] Docker build workflow (multi-platform, SBOM)
- [x] PR checks workflow (labeling, validation)

**GitHub Actions Files Created:**
```
.github/
├── workflows/
│   ├── ci.yml           # Build and test on push/PR
│   ├── cd.yml           # Deploy to AWS on merge to main
│   ├── docker-build.yml # Build/publish Docker images
│   └── pr-checks.yml    # PR validation and labeling
├── labeler.yml          # Auto-labeling configuration
└── README.md            # CI/CD documentation
```

**CI Workflow Features:**
- Matrix build for all microservices
- Maven dependency caching
- Unit and integration tests
- Code quality checks (Checkstyle, SpotBugs)
- Docker build validation

**CD Workflow Features:**
- Push Docker images to AWS ECR
- Deploy via AWS SSM Session Manager
- Environment-based deployments (dev, staging, prod)
- Automatic rollback on failure
- Health check verification

**Docker Build Workflow Features:**
- Multi-platform builds (amd64, arm64)
- Semantic versioning with tags
- SBOM (Software Bill of Materials) generation
- Trivy security scanning
- GitHub Releases creation

**Required Secrets:**
```
AWS_ACCOUNT_ID
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
GITHUB_TOKEN (auto-provided)
```

---

## 🐛 Known Issues / Notes

1. **S3 Health Indicator**: Will fail if S3 bucket doesn't exist (expected in local dev)
2. **Kafka**: Requires Kafka broker running for full functionality
3. **Security**: Services trust API Gateway for auth; add defense-in-depth for production

---

## 🔧 Bug Fixes Applied (January 2025)

The following compilation errors were identified and fixed to ensure the project compiles successfully:

### 1. Missing Spring Security Dependency
**Issue:** `product-service` and `order-service` used `@PreAuthorize` but lacked Spring Security dependency.
**Fix:** Added `spring-boot-starter-security` and `spring-security-test` to both services' `pom.xml`.

### 2. Lombok @SuperBuilder for Entity Inheritance
**Issue:** Entity builders couldn't access inherited fields (e.g., `.id()`) because Lombok's `@Builder` doesn't include parent class fields.
**Fix:** Changed all entities and base classes to use `@SuperBuilder`:
- `BaseEntity.java` → `@SuperBuilder` + `@NoArgsConstructor`
- `AuditableEntity.java` → `@SuperBuilder` + `@NoArgsConstructor`
- `User.java`, `Product.java`, `Category.java`, `Inventory.java`, `Order.java`, `OrderItem.java` → `@SuperBuilder`

### 3. Money.of() Method Signature
**Issue:** Tests passed `String` currency code but `Money.of()` only accepted `CurrencyCode` enum.
**Fix:** Added overloaded factory methods in `Money.java`:
```java
public static Money of(BigDecimal amount, String currencyCode)
public static Money of(double amount, String currencyCode)
public static Money of(BigDecimal amount) // defaults to USD
```

### 4. PageResponse.of() Method Signature
**Issue:** Controllers called `PageResponse.of(Page<T>)` but only `of(List, int, int, long)` existed.
**Fix:** Added `of(Page<T>)` method in `PageResponse.java` that delegates to `from(Page<T>)`.

### 5. ProductSpecification Type Issues
**Issue:** `CriteriaBuilder.between()` failed due to untyped `Path<Object>` for price queries.
**Fix:** Explicitly typed `Path<BigDecimal>` in `ProductSpecification.java`:
```java
jakarta.persistence.criteria.Path<BigDecimal> priceAmount = root.get("price").get("amount");
```

### 6. Missing Inventory Methods
**Issue:** Tests referenced `hasStock(int)` and `isOutOfStock()` methods that didn't exist.
**Fix:** Added methods to `Inventory.java`:
```java
public boolean hasStock(int amount) { return getAvailableQuantity() >= amount; }
public boolean isOutOfStock() { return getAvailableQuantity() <= 0; }
```

### 7. Missing Order Methods and Fields
**Issue:** Tests referenced `markPaymentReceived()`, `getItemCount()`, and `cancelledAt` field.
**Fix:** Added to `Order.java`:
- `cancelledAt` field with `@Column`
- `markPaymentReceived()` method (alias for `markPaymentCompleted()`)
- `getItemCount()` method that sums item quantities
- Updated `cancel()` to set `cancelledAt` timestamp

### 8. Address zipCode Alias
**Issue:** Tests used `zipCode()` builder method but class had `postalCode`.
**Fix:** Added custom builder classes with `zipCode()` alias in:
- `Address.java` (domain value object)
- `AddressRequest.java` (DTO)

Also added `getZipCode()` getter alias in `Address.java`.

### 9. Ambiguous Repository Delete Method
**Issue:** `verify(userRepository, never()).delete(any())` was ambiguous (could match `delete(User)` or `delete(Specification<User>)`).
**Fix:** Changed to `delete(any(User.class))` in `UserServiceImplTest.java`.

### 10. ArchUnit API Method
**Issue:** `mayBeAccessedByAnyLayer()` method doesn't exist in ArchUnit API.
**Fix:** Removed the constraint in `ArchitectureTest.java` - Domain layer is accessible by default without explicit rule.

---

### Summary of Modified Files

| Category | Files Modified |
|----------|----------------|
| **POMs** | `product-service/pom.xml`, `order-service/pom.xml` |
| **Base Classes** | `BaseEntity.java`, `AuditableEntity.java` |
| **Entities** | `User.java`, `Product.java`, `Category.java`, `Inventory.java`, `Order.java`, `OrderItem.java` |
| **Value Objects** | `Money.java`, `Address.java` |
| **DTOs** | `PageResponse.java`, `AddressRequest.java` |
| **Specifications** | `ProductSpecification.java` |
| **Tests** | `UserServiceImplTest.java`, `ArchitectureTest.java` |

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

### Terraform Concepts (Phase 5)
| Concept | Example File | Description |
|---------|--------------|-------------|
| Provider Configuration | `main.tf` | AWS provider with default tags |
| Variable Validation | `variables.tf` | Input validation with regex |
| Local Values | `main.tf` | Computed values for DRY code |
| Data Sources | `main.tf` | Query existing AWS resources |
| Resource Dependencies | `ec2.tf` | Implicit and explicit depends_on |
| Dynamic Blocks | `vpc.tf`, `security_groups.tf` | Conditional resource creation |
| Template Files | `templates/user_data.sh` | EC2 bootstrap scripts |
| Output Values | `outputs.tf` | Export infrastructure details |
| Secrets Manager | `rds.tf` | Secure credential storage |
| IAM Policies | `iam.tf` | Least privilege access |

### CI/CD Concepts (Phase 6)
| Concept | Example File | Description |
|---------|--------------|-------------|
| Matrix Strategy | `ci.yml` | Parallel builds for multiple services |
| Dependency Caching | All workflows | Cache Maven dependencies for speed |
| Job Dependencies | `ci.yml` | Build common module before services |
| Artifact Upload/Download | `ci.yml` | Share build outputs between jobs |
| GitHub Environments | `cd.yml` | Environment-specific secrets and protection |
| AWS ECR Login | `cd.yml` | Authenticate to container registry |
| SSM Send Command | `cd.yml` | Execute scripts on EC2 securely |
| Multi-platform Builds | `docker-build.yml` | Build for amd64 and arm64 |
| SBOM Generation | `docker-build.yml` | Software Bill of Materials for security |
| Trivy Security Scan | `docker-build.yml` | Container vulnerability scanning |
| Conventional Commits | `pr-checks.yml` | Enforce commit message standards |
| PR Auto-labeling | `labeler.yml` | Label PRs based on changed files |

---

## 🎉 Project Complete!

All 6 phases have been implemented:

1. **Foundation** - Multi-module Maven, Eureka, Gateway
2. **Services** - User, Product, Order microservices
3. **Kafka** - Event-driven architecture
4. **Testing** - Unit, Integration, Architecture tests
5. **Terraform** - AWS Infrastructure as Code
6. **CI/CD** - GitHub Actions pipelines

The project demonstrates:
- Spring Boot 3.x best practices
- PEAA (Patterns of Enterprise Application Architecture)
- Microservices patterns (Service Discovery, API Gateway, Event-Driven)
- AWS cloud infrastructure
- Modern CI/CD practices

---

*Document auto-updated after each phase completion*
