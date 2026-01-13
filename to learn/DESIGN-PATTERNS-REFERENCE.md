# MicroMart - Design Patterns Reference

A comprehensive guide to all design patterns implemented in this project.

---

## Overview

MicroMart implements patterns from three main sources:
1. **PEAA** - Patterns of Enterprise Application Architecture (Martin Fowler)
2. **DDD** - Domain-Driven Design (Eric Evans)
3. **GoF** - Gang of Four Design Patterns

---

## PEAA Patterns (Enterprise Architecture)

### 1. Repository Pattern

**What it does**: Mediates between the domain and data mapping layers.

**Where used**: All `*Repository` interfaces

**Example**:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailValue(String email);
    boolean existsByUsername(String username);
}
```

**Benefits**:
- Decouples business logic from data access
- Enables switching databases without changing services
- Provides a collection-like interface for data

---

### 2. Service Layer Pattern

**What it does**: Defines an application's boundary with a layer of services.

**Where used**: All `*ServiceImpl` classes

**Example**:
```java
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Validate
        // 2. Reserve inventory
        // 3. Calculate total
        // 4. Save order
        // 5. Publish event
    }
}
```

**Benefits**:
- Encapsulates business logic
- Coordinates multiple repositories
- Manages transactions

---

### 3. Data Transfer Object (DTO)

**What it does**: An object that carries data between processes.

**Where used**: All `*Request` and `*Response` classes

**Example**:
```java
// Request DTO - for input
public record CreateUserRequest(
    @NotBlank String username,
    @Email String email,
    @Size(min = 8) String password
) {}

// Response DTO - for output
public record UserResponse(
    Long id,
    String username,
    String email,
    Set<String> roles
) {}
```

**Benefits**:
- Decouples API contract from domain model
- Hides internal entity structure
- Enables versioning of API

---

### 4. Domain Model

**What it does**: An object model of the domain that incorporates both behavior and data.

**Where used**: All entity classes with business methods

**Example**:
```java
@Entity
public class Order {
    // Data
    private OrderStatus status;
    private List<OrderItem> items;

    // Behavior
    public void confirm() {
        if (status != PENDING) {
            throw new IllegalStateException("Can only confirm pending orders");
        }
        this.status = CONFIRMED;
    }

    public boolean canBeCancelled() {
        return status == PENDING || status == CONFIRMED;
    }

    public BigDecimal calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

**Benefits**:
- Business logic lives with the data
- Self-validating entities
- Better encapsulation

---

### 5. Gateway Pattern

**What it does**: Provides a single entry point to a complex subsystem.

**Where used**: API Gateway

**Example**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**,/api/auth/**
```

**Benefits**:
- Single point for authentication
- Request routing and load balancing
- Cross-cutting concerns (logging, rate limiting)

---

### 6. Registry Pattern

**What it does**: A well-known object that other objects use to find common objects.

**Where used**: Eureka Server

**Example**:
```java
@SpringBootApplication
@EnableEurekaServer  // Makes this the registry
public class EurekaServerApplication { }

// In client services:
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Benefits**:
- Dynamic service discovery
- No hardcoded URLs
- Enables load balancing

---

### 7. Optimistic Offline Lock

**What it does**: Prevents conflicts by detecting a conflict and rolling back.

**Where used**: Inventory entity

**Example**:
```java
@Entity
public class Inventory {
    @Version
    private Long version;  // Incremented on each update

    public void reserve(int quantity) {
        if (quantity > getAvailableQuantity()) {
            throw new InsufficientStockException();
        }
        this.reservedQuantity += quantity;
        // Hibernate checks version before commit
        // Throws OptimisticLockException if version changed
    }
}
```

**Benefits**:
- Prevents lost updates
- No database locks needed
- Better performance than pessimistic locking

---

## DDD Patterns (Domain-Driven Design)

### 8. Value Object

**What it does**: A small, immutable object that describes some characteristic.

**Where used**: `Email`, `Money`, `Address`

**Example**:
```java
@Embeddable
public final class Money implements Comparable<Money> {
    private final BigDecimal amount;
    private final CurrencyCode currency;

    // Factory method (no public constructor)
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        return new Money(amount, currency);
    }

    // Immutable operations return new instances
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // Equality by value
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Money other)) return false;
        return amount.compareTo(other.amount) == 0
            && currency == other.currency;
    }
}
```

**Characteristics**:
- Immutable
- Equality by value (not identity)
- Self-validating
- No side effects

---

### 9. Entity

**What it does**: An object defined primarily by its identity.

**Where used**: `User`, `Product`, `Order`

**Example**:
```java
@Entity
public class User extends AuditableEntity {
    @Id
    @GeneratedValue
    private Long id;  // Identity

    // State changes over time
    private boolean enabled;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;

    // Equality by ID
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }
}
```

**Difference from Value Object**:
- Has unique identity (id)
- Mutable
- Equality by identity

---

### 10. Aggregate

**What it does**: A cluster of entities and value objects with a defined boundary.

**Where used**: `Order` (with `OrderItem`), `Product` (with `Inventory`)

**Example**:
```java
@Entity
public class Order {  // Aggregate Root
    @Id
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;  // Part of aggregate

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }
}
```

**Rules**:
- Only aggregate root is accessed from outside
- All changes go through the root
- Ensures consistency

---

### 11. Bounded Context

**What it does**: Defines the boundaries of a model's applicability.

**Where used**: Each microservice

**Example**:
```
User Context (user-service):
  - User, Role, Email, Authentication

Product Context (product-service):
  - Product, Category, Inventory, Money

Order Context (order-service):
  - Order, OrderItem, Address
```

**Benefits**:
- Clear separation of concerns
- Teams can work independently
- Different models for different contexts

---

## GoF Patterns (Gang of Four)

### 12. Factory Method

**What it does**: Creates objects without specifying the exact class.

**Where used**: Value object creation

**Example**:
```java
public class Money {
    // Private constructor
    private Money(BigDecimal amount, CurrencyCode currency) { }

    // Factory methods
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        return new Money(amount, currency);
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, CurrencyCode.USD);
    }

    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
```

**Benefits**:
- Validation before creation
- Descriptive naming
- Can return subclasses

---

### 13. Strategy Pattern

**What it does**: Defines a family of algorithms and makes them interchangeable.

**Where used**: NotificationService implementations

**Example**:
```java
// Strategy interface
public interface NotificationService {
    void sendNotification(String recipient, String message);
}

// Concrete strategies
@Component
public class EmailNotificationService implements NotificationService {
    public void sendNotification(String recipient, String message) {
        // Send email
    }
}

@Component
public class SmsNotificationService implements NotificationService {
    public void sendNotification(String recipient, String message) {
        // Send SMS
    }
}

// Context
@Component
public class UserNotificationFacade {
    private final EmailNotificationService emailService;

    public void notifyUserRegistered(User user) {
        emailService.sendNotification(user.getEmail(), "Welcome!");
    }
}
```

**Benefits**:
- Easy to add new notification types
- Runtime switching
- Testable with mocks

---

### 14. Observer Pattern

**What it does**: Defines a one-to-many dependency between objects.

**Where used**: Event publishing/listening

**Example**:
```java
// Publisher
@Component
public class UserEventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publishUserRegistered(User user) {
        publisher.publishEvent(new UserRegisteredEvent(user));
    }
}

// Observer (Listener)
@Component
public class UserEventListener {
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: {}", event.getUsername());
        // Send welcome email
    }
}
```

**Benefits**:
- Loose coupling between publisher and listeners
- Multiple listeners for one event
- Easy to add new reactions

---

### 15. Builder Pattern

**What it does**: Separates object construction from its representation.

**Where used**: DTOs and entities (via Lombok)

**Example**:
```java
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String categoryName;
}

// Usage
ProductResponse response = ProductResponse.builder()
    .id(product.getId())
    .name(product.getName())
    .price(product.getPrice().getAmount())
    .categoryName(product.getCategory().getName())
    .build();
```

**Benefits**:
- Readable object construction
- Optional parameters
- Immutable objects

---

## Resilience Patterns

### 16. Circuit Breaker

**What it does**: Prevents cascade failures by failing fast when a service is down.

**Where used**: Feign clients

**Example**:
```java
@FeignClient(
    name = "product-service",
    fallback = ProductClientFallback.class
)
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}

@Component
public class ProductClientFallback implements ProductClient {
    public ProductDto getProduct(Long id) {
        throw new ServiceUnavailableException("Product service down");
    }
}
```

**States**:
- **Closed**: Normal operation, requests go through
- **Open**: Service is down, fail immediately
- **Half-Open**: Testing if service recovered

---

## Architectural Patterns

### 17. Microservices Architecture

**What it does**: Structures an application as a collection of loosely coupled services.

**Implementation**:
```
          ┌─────────────────────┐
          │     API Gateway     │
          └──────────┬──────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
┌─────┴─────┐  ┌─────┴─────┐  ┌─────┴─────┐
│User Service│  │Product Svc│  │Order Svc  │
└─────┬─────┘  └─────┬─────┘  └─────┬─────┘
      │              │              │
  ┌───┴───┐      ┌───┴───┐      ┌───┴───┐
  │user_db│      │prod_db│      │order_db│
  └───────┘      └───────┘      └───────┘
```

**Characteristics**:
- Each service has its own database
- Services communicate via APIs/events
- Independent deployment
- Technology flexibility

---

### 18. Event-Driven Architecture

**What it does**: Uses events to trigger and communicate between services.

**Implementation**:
```
┌───────────┐    ┌───────┐    ┌───────────┐
│Order Svc  │───►│ Kafka │◄───│Product Svc│
└───────────┘    └───────┘    └───────────┘
     │               │              │
     └─ OrderCreated ┼ StockReserved─┘
```

**Benefits**:
- Loose coupling
- Asynchronous processing
- Audit trail
- Scalability

---

## Pattern Relationships

```
┌──────────────────────────────────────────────────────────────┐
│                    Microservices Architecture                 │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Per Service:                          │ │
│  │  ┌─────────┐    ┌───────────┐    ┌────────────┐        │ │
│  │  │Controller│───►│ Service   │───►│ Repository │        │ │
│  │  │  (DTO)  │    │ (Domain   │    │  (Entity)  │        │ │
│  │  └─────────┘    │  Model)   │    └────────────┘        │ │
│  │                 │  (Value   │                           │ │
│  │                 │  Objects) │                           │ │
│  │                 └───────────┘                           │ │
│  │        │                    │                            │ │
│  │        │ Events             │ Feign Client               │ │
│  │        ▼                    ▼                            │ │
│  │  ┌──────────┐        ┌─────────────┐                    │ │
│  │  │  Kafka   │        │Other Service│                    │ │
│  │  │ (Observer)│        │(Circuit Brk)│                    │ │
│  │  └──────────┘        └─────────────┘                    │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

## Quick Reference Table

| Pattern | Category | Main Benefit |
|---------|----------|--------------|
| Repository | PEAA | Decouple data access |
| Service Layer | PEAA | Encapsulate business logic |
| DTO | PEAA | Decouple API from domain |
| Domain Model | PEAA | Rich behavior in entities |
| Gateway | PEAA | Single entry point |
| Registry | PEAA | Service discovery |
| Optimistic Lock | PEAA | Concurrency control |
| Value Object | DDD | Immutable domain concepts |
| Entity | DDD | Persistent identity |
| Aggregate | DDD | Consistency boundary |
| Bounded Context | DDD | Clear separation |
| Factory Method | GoF | Controlled creation |
| Strategy | GoF | Interchangeable algorithms |
| Observer | GoF | Loose coupling |
| Builder | GoF | Readable construction |
| Circuit Breaker | Resilience | Fault tolerance |
| Microservices | Architecture | Independent deployment |
| Event-Driven | Architecture | Async communication |

---

*Understanding these patterns will help you maintain and extend the codebase effectively.*
