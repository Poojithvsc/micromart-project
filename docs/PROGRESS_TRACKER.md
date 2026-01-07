# MicroMart - Progress Tracker

## Original Requirements (From User Prompt)

### Technologies Requested
| Technology | Status | Location/Notes |
|------------|--------|----------------|
| Java 17 | ✅ Done | All modules |
| Spring Boot 3.2.x | ✅ Done | Parent POM |
| API (REST) | ✅ Done | Controllers in all services |
| Kafka | 🔲 Pending | Config exists, producers/consumers needed |
| Docker | ✅ Done | Dockerfiles + docker-compose |
| PostgreSQL | ✅ Done | Configured in all services |
| S3 Bucket | 🔲 Pending | AWS SDK in product-service, implementation needed |
| Unit Tests | 🔲 Pending | Test dependencies added, tests needed |
| Terraform | 🔲 Pending | Phase 8 |
| AWS (EC2, RDS via Terraform) | 🔲 Pending | Phase 8 |
| GitHub Actions CI/CD | 🔲 Pending | Phase 9 |

### Spring Boot Concepts Requested
| Concept | Status | Location |
|---------|--------|----------|
| **Core Stereotypes** | | |
| @Controller/@RestController | ✅ Done | UserController, FallbackController |
| @Service | ✅ Done | UserServiceImpl |
| @Repository | ✅ Done | UserRepository |
| @Component | ✅ Done | Filters, Fallbacks |
| @Entity | ✅ Done | User, Product, Order, Category |
| @Embeddable | ✅ Done | Email, Money, Address |
| **Spring Data JPA** | | |
| JpaRepository | ✅ Done | UserRepository |
| @Query | ✅ Done | UserRepository custom queries |
| @EntityGraph | ✅ Done | UserRepository.findByEmailValue() |
| @Transactional | ✅ Done | UserServiceImpl |
| Specification | 🔲 Pending | ProductSpecification needed |
| @ManyToOne/@OneToMany | ✅ Done | Product-Category, Order-OrderItem |
| **Dependency Injection** | | |
| Constructor injection | ✅ Done | All services use @RequiredArgsConstructor |
| @Autowired | ✅ Done | (via constructor injection) |
| @Qualifier | 🔲 Pending | Need example |
| @Primary | 🔲 Pending | Need example |
| @Profile | ✅ Done | application.yml profiles |
| @Conditional | 🔲 Pending | Need example |
| **Spring MVC** | | |
| @GetMapping/@PostMapping | ✅ Done | UserController |
| @RequestBody | ✅ Done | UserController |
| @Valid | ✅ Done | UserController |
| @ExceptionHandler | ✅ Done | GlobalExceptionHandler |
| @ControllerAdvice | ✅ Done | GlobalExceptionHandler |
| **Configuration** | | |
| application.yml | ✅ Done | All services |
| @Value | 🔲 Pending | Need example |
| @ConfigurationProperties | ✅ Done | JwtConfig |
| profiles (dev/prod) | ✅ Done | All application.yml files |
| **Security** | | |
| SecurityFilterChain | ✅ Done | SecurityConfig in services |
| @PreAuthorize | ✅ Done | UserController |
| @Secured | 🔲 Pending | Need example |
| JWT | ✅ Done | API Gateway filter |
| OAuth2 | 🔲 Pending | Optional enhancement |
| **Spring Cloud** | | |
| @EnableEurekaClient | ✅ Done | All services (@EnableDiscoveryClient) |
| @FeignClient | ✅ Done | ProductClient in order-service |
| Circuit Breaker | ✅ Done | Resilience4j config, FallbackController |
| Spring Cloud Gateway | ✅ Done | api-gateway |
| Stream | 🔲 Pending | Kafka implementation |
| **Events** | | |
| ApplicationEvent | 🔲 Pending | Need implementation |
| @EventListener | 🔲 Pending | Need implementation |
| @TransactionalEventListener | 🔲 Pending | Need implementation |
| @Async | 🔲 Pending | Enabled, need usage |
| **Actuator** | | |
| Health checks | ✅ Done | Configured in all services |
| Metrics | ✅ Done | Exposed in actuator |
| Custom indicators | 🔲 Pending | Need implementation |

### Martin Fowler's PEAA Patterns Requested
| Pattern | Status | Location |
|---------|--------|----------|
| Domain Model | ✅ Done | User, Product, Order with behavior |
| Data Mapper | ✅ Done | JPA/Hibernate |
| Repository | ✅ Done | Spring Data JPA repositories |
| Service Layer | ✅ Done | UserService/UserServiceImpl |
| Unit of Work | ✅ Done | @Transactional |
| Identity Map | ✅ Done | JPA EntityManager (implicit) |
| Lazy Load | ✅ Done | FetchType.LAZY, @EntityGraph |
| Value Object | ✅ Done | Email, Money, Address |
| DTO | ✅ Done | ApiResponse, Request/Response DTOs |
| Remote Facade | ✅ Done | REST Controllers |
| Gateway | ✅ Done | API Gateway |
| Registry | ✅ Done | Eureka Server |
| Plugin | 🔲 Pending | @Conditional example needed |
| Separated Interface | ✅ Done | UserService interface |
| Money Pattern | ✅ Done | Money.java in product-service |
| Specification | 🔲 Pending | JPA Specification needed |

### Fowler's Layers → Spring Mapping (Requested)
| Fowler's Layer | Spring Stereotypes | Status |
|----------------|-------------------|--------|
| Presentation | @Controller, @RestController, Spring MVC | ✅ Done |
| Service/Application | @Service, @Transactional, Events | ✅ Partial (Events pending) |
| Domain | @Entity, @Embeddable, domain objects | ✅ Done |
| Data Source/Infrastructure | @Repository, Spring Data JPA, Kafka, S3 | ✅ Partial (Kafka/S3 pending) |

---

## Implementation Phases

### Phase 1: Project Foundation ✅ COMPLETE
- [x] Parent POM with dependency management
- [x] Common module (DTOs, exceptions, events, base entities)
- [x] Eureka Server
- [x] API Gateway with JWT filter
- [x] User Service structure
- [x] Product Service structure
- [x] Order Service structure
- [x] Docker Compose

### Phase 2: Complete Service Implementations 🔲 PENDING
- [ ] Complete UserService with full JWT auth (login, register, token refresh)
- [ ] Complete ProductService with CRUD, S3 image upload
- [ ] Complete OrderService with order workflow
- [ ] Add JPA Specifications for dynamic queries
- [ ] Add @Qualifier, @Primary, @Conditional examples
- [ ] Add @Value examples
- [ ] Add ApplicationEvent and @EventListener

### Phase 3: Kafka Integration 🔲 PENDING
- [ ] Kafka producer in order-service
- [ ] Kafka consumer in product-service (inventory updates)
- [ ] Event-driven order workflow
- [ ] Dead letter queue handling

### Phase 4: Testing 🔲 PENDING
- [ ] Unit tests with Mockito
- [ ] Integration tests with Testcontainers
- [ ] Architecture tests with ArchUnit
- [ ] API tests for controllers

### Phase 5: Terraform & AWS 🔲 PENDING
- [ ] VPC configuration
- [ ] EC2 instance setup
- [ ] RDS PostgreSQL
- [ ] S3 bucket
- [ ] Security groups
- [ ] IAM roles

### Phase 6: CI/CD 🔲 PENDING
- [ ] GitHub Actions build workflow
- [ ] GitHub Actions test workflow
- [ ] Docker image publishing
- [ ] Deployment pipeline

---

## Git Workflow (CRITICAL - ALWAYS FOLLOW!)

```
feature/xyz → dev → PR → main
```

| Rule | Action |
|------|--------|
| **Working branch** | Always `dev` (NEVER push directly to main) |
| **Before starting** | `git checkout dev && git pull origin dev` |
| **Push to** | `dev` branch only |
| **Merge to main** | Create Pull Request from dev → main |
| **Feature work** | Optionally create feature/xyz from dev |

**IMPORTANT:** Main branch is production-ready only. All development work happens on dev.

---

## Quick Start for New Session

```
Continue building the MicroMart project.
Repository: https://github.com/Poojithvsc/micromart-project

⚠️  IMPORTANT: Always work on dev branch, NOT main!
    Run: git checkout dev && git pull origin dev

Phase 1 is COMPLETE. Start Phase 2.

Key files to check:
- docs/PROJECT_PLAN.md (architecture)
- docs/PROGRESS_TRACKER.md (this file - detailed progress)
- pom.xml (parent POM)

Focus on completing the 🔲 PENDING items above, especially:
1. Spring Boot concepts not yet implemented
2. PEAA patterns not yet demonstrated
3. Kafka integration
4. Testing
5. Terraform/AWS
6. GitHub Actions
```

---

## Files Created in Phase 1

Total: 66 files, 5,818 lines of code

### By Module
- common/: 12 files
- eureka-server/: 4 files
- api-gateway/: 8 files
- user-service/: 15 files
- product-service/: 6 files
- order-service/: 9 files
- infrastructure/docker/: 3 files
- Root: pom.xml, docs/

---

*Last Updated: Phase 1 Complete*
