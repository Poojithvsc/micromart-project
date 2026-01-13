# MicroMart - Low Level Design Document

## Table of Contents
1. [Executive Summary](#1-executive-summary)
2. [Project Overview](#2-project-overview)
3. [System Architecture](#3-system-architecture)
4. [Services Deep Dive](#4-services-deep-dive)
5. [Domain Model](#5-domain-model)
6. [API Specifications](#6-api-specifications)
7. [Design Patterns](#7-design-patterns)
8. [Security Architecture](#8-security-architecture)
9. [Data Architecture](#9-data-architecture)
10. [Event-Driven Architecture](#10-event-driven-architecture)
11. [Technology Stack](#11-technology-stack)
12. [Deployment Architecture](#12-deployment-architecture)

---

## 1. Executive Summary

### What is MicroMart?

MicroMart is a **fully-functional e-commerce platform** built using **microservices architecture**. Think of it as a mini Amazon or Flipkart - it allows users to:

- **Register and Login** securely
- **Browse Products** organized by categories
- **Place Orders** with automatic inventory management
- **Track Order Status** from placement to delivery

### Why Microservices?

Instead of one large application (monolith), MicroMart is split into **independent services** that:
- Can be developed by different teams
- Can be deployed independently
- Can scale based on demand (e.g., scale Order Service during sales)
- Are more resilient (one service failure doesn't crash everything)

### Key Business Capabilities

| Capability | Service | Description |
|------------|---------|-------------|
| User Management | User Service | Registration, login, profile management |
| Authentication | User Service | JWT-based secure authentication |
| Product Catalog | Product Service | Product listing, search, categories |
| Inventory | Product Service | Stock tracking, reservations |
| Order Processing | Order Service | Order creation, lifecycle management |
| Service Discovery | Eureka Server | Services find each other dynamically |
| API Routing | API Gateway | Single entry point for all APIs |

---

## 2. Project Overview

### 2.1 Business Context

MicroMart solves the challenge of building a **scalable, maintainable e-commerce platform**. It demonstrates:

1. **Separation of Concerns**: Each service handles one business domain
2. **Independent Scalability**: Scale only what needs scaling
3. **Technology Flexibility**: Services can use different technologies
4. **Team Independence**: Teams can work on different services

### 2.2 Functional Requirements

#### User Management
- User registration with email verification
- Secure login with JWT tokens
- Profile management
- Role-based access (USER, ADMIN, MODERATOR)
- Account locking after failed attempts

#### Product Management
- CRUD operations for products and categories
- Product search with filters (name, category, price range)
- Image upload to AWS S3
- SKU-based product identification

#### Inventory Management
- Real-time stock tracking
- Stock reservation during order placement
- Automatic low-stock alerts
- Optimistic locking for concurrent updates

#### Order Management
- Order creation with multiple items
- Order lifecycle: PENDING → CONFIRMED → SHIPPED → DELIVERED
- Order cancellation with inventory release
- Order history per user

### 2.3 Non-Functional Requirements

| Requirement | Target | Implementation |
|-------------|--------|----------------|
| Availability | 99.9% | Multiple service instances, health checks |
| Response Time | < 500ms | Caching, optimized queries |
| Scalability | 10x growth | Horizontal scaling via Kubernetes |
| Security | Enterprise-grade | JWT, HTTPS, input validation |
| Maintainability | High | Clean architecture, documentation |

---

## 3. System Architecture

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│    (Web Browser, Mobile App, Third-party Applications)          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Port 8080)                     │
│  • Request Routing          • Authentication Filter              │
│  • Rate Limiting            • Circuit Breaker                    │
│  • Load Balancing           • CORS Handling                      │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ USER SERVICE  │     │ PRODUCT SERVICE │     │  ORDER SERVICE  │
│  (Port 8081)  │     │   (Port 8082)   │     │   (Port 8083)   │
├───────────────┤     ├─────────────────┤     ├─────────────────┤
│ • Auth        │     │ • Products      │     │ • Orders        │
│ • Users       │     │ • Categories    │     │ • Order Items   │
│ • Roles       │     │ • Inventory     │     │ • Lifecycle     │
└───────┬───────┘     └────────┬────────┘     └────────┬────────┘
        │                      │                       │
        ▼                      ▼                       ▼
   ┌─────────┐           ┌─────────┐             ┌─────────┐
   │ user_db │           │product_db│             │order_db │
   │(Postgres)│           │(Postgres)│             │(Postgres)│
   └─────────┘           └─────────┘             └─────────┘
```

### 3.2 Service Communication

```
                    ┌──────────────────────────┐
                    │     EUREKA SERVER        │
                    │      (Port 8761)         │
                    │   Service Discovery      │
                    └──────────────────────────┘
                           ▲    ▲    ▲
            ┌──────────────┘    │    └──────────────┐
            │                  │                    │
     Register/Discover   Register/Discover   Register/Discover
            │                  │                    │
    ┌───────┴───────┐  ┌──────┴───────┐  ┌────────┴────────┐
    │ User Service  │  │Product Service│  │  Order Service  │
    └───────────────┘  └──────────────┘  └────────┬────────┘
                              ▲                    │
                              │    Feign Client    │
                              └────────────────────┘
                              (Get Product, Reserve Stock)
```

### 3.3 Event Flow (Kafka)

```
┌─────────────────────────────────────────────────────────────────┐
│                        APACHE KAFKA                              │
│                    Message Broker Cluster                        │
└─────────────────────────────────────────────────────────────────┘
        ▲                    ▲                    ▲
        │                    │                    │
   ┌────┴────┐          ┌────┴────┐          ┌────┴────┐
   │user-    │          │inventory│          │order-   │
   │events   │          │-events  │          │events   │
   └────┬────┘          └────┬────┘          └────┬────┘
        │                    │                    │
   Publishes:           Publishes:           Publishes:
   • UserRegistered     • StockReserved      • OrderCreated
   • UserLoggedIn       • LowStock           • OrderShipped
   • AccountLocked      • StockReleased      • OrderDelivered
```

---

## 4. Services Deep Dive

### 4.1 User Service (Port 8081)

**Purpose**: Handles all user-related operations including authentication.

#### Components

```
user-service/
├── controller/
│   ├── UserController.java      # User CRUD endpoints
│   └── AuthController.java      # Login, register, token refresh
├── service/
│   ├── UserService.java         # User business logic interface
│   ├── UserServiceImpl.java     # User business logic
│   ├── AuthService.java         # Auth business logic interface
│   └── AuthServiceImpl.java     # Auth business logic
├── repository/
│   └── UserRepository.java      # Database operations
├── domain/
│   ├── User.java               # User entity
│   ├── Role.java               # Role enum
│   └── valueobject/
│       └── Email.java          # Email value object
├── dto/
│   ├── request/                # Input DTOs
│   └── response/               # Output DTOs
├── security/
│   ├── JwtTokenProvider.java   # JWT generation/validation
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── mapper/
│   └── UserMapper.java         # Entity ↔ DTO conversion
├── event/
│   ├── UserEventPublisher.java
│   └── UserEventListener.java
└── config/
    ├── SecurityConfig.java
    └── JwtConfig.java
```

#### Key Features

1. **JWT Authentication**
   - Access Token: 24 hours validity
   - Refresh Token: 7 days validity
   - Stateless authentication

2. **Role-Based Access Control**
   ```java
   public enum Role {
       USER("ROLE_USER"),
       ADMIN("ROLE_ADMIN"),
       MODERATOR("ROLE_MODERATOR");
   }
   ```

3. **Account Security**
   - Password hashing with BCrypt
   - Account locking after 5 failed attempts
   - Last login tracking

### 4.2 Product Service (Port 8082)

**Purpose**: Manages product catalog, categories, and inventory.

#### Components

```
product-service/
├── controller/
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── InventoryController.java
│   └── ProductImageController.java
├── service/
│   ├── ProductService.java / ProductServiceImpl.java
│   ├── CategoryService.java / CategoryServiceImpl.java
│   ├── InventoryService.java / InventoryServiceImpl.java
│   └── S3StorageService.java / S3StorageServiceImpl.java
├── repository/
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   └── InventoryRepository.java
├── domain/
│   ├── Product.java
│   ├── Category.java
│   ├── Inventory.java
│   └── valueobject/
│       └── Money.java
├── dto/
│   ├── request/
│   └── response/
├── mapper/
│   ├── ProductMapper.java
│   ├── CategoryMapper.java
│   └── InventoryMapper.java
├── event/
│   ├── InventoryEventPublisher.java
│   └── InventoryEventListener.java
└── config/
    ├── S3Config.java
    └── KafkaConsumerConfig.java
```

#### Key Features

1. **Product Search with Specifications**
   ```java
   // Dynamic query building
   ProductSearchRequest {
       String name;           // LIKE search
       Long categoryId;       // Exact match
       BigDecimal minPrice;   // >= filter
       BigDecimal maxPrice;   // <= filter
       Boolean active;        // Exact match
   }
   ```

2. **Inventory Management**
   - Optimistic locking with `@Version`
   - Stock reservation for orders
   - Automatic low-stock alerts

3. **S3 Image Storage**
   - Upload product images to AWS S3
   - Generate pre-signed URLs for secure access
   - Support for JPEG, PNG, GIF, WebP

### 4.3 Order Service (Port 8083)

**Purpose**: Handles order creation, lifecycle, and history.

#### Components

```
order-service/
├── controller/
│   └── OrderController.java
├── service/
│   ├── OrderService.java
│   └── OrderServiceImpl.java
├── repository/
│   └── OrderRepository.java
├── domain/
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   └── valueobject/
│       └── Address.java
├── dto/
│   ├── request/
│   └── response/
├── mapper/
│   └── OrderMapper.java
├── client/
│   ├── ProductClient.java         # Feign client
│   ├── ProductClientFallback.java
│   ├── InventoryClient.java
│   └── InventoryClientFallback.java
└── kafka/
    ├── OrderEventProducer.java
    └── event/
        └── OrderEvent.java
```

#### Key Features

1. **Order Lifecycle**
   ```
   PENDING → CONFIRMED → PAYMENT_COMPLETED → PROCESSING → SHIPPED → DELIVERED
                ↓
            CANCELLED (if allowed)
   ```

2. **Inter-Service Communication**
   - Feign clients for Product and Inventory services
   - Circuit breaker for fault tolerance
   - Fallback handlers for graceful degradation

3. **Order Number Generation**
   - Format: `ORD-{UUID}`
   - Unique, indexed, human-readable

### 4.4 API Gateway (Port 8080)

**Purpose**: Single entry point for all client requests.

#### Responsibilities

1. **Request Routing**
   ```yaml
   routes:
     /api/users/**    → user-service
     /api/auth/**     → user-service
     /api/products/** → product-service
     /api/orders/**   → order-service
   ```

2. **Authentication Filter**
   - Validates JWT on protected routes
   - Adds user context headers (X-User-Id, X-User-Roles)
   - Skips public endpoints (/auth/login, /auth/register)

3. **Cross-Cutting Concerns**
   - Rate limiting
   - CORS handling
   - Circuit breaker
   - Request/Response logging

### 4.5 Eureka Server (Port 8761)

**Purpose**: Service discovery and registration.

#### How It Works

1. Services register on startup with their address
2. Services query Eureka to find other services
3. Health checks ensure only healthy services receive traffic
4. Load balancing across multiple instances

---

## 5. Domain Model

### 5.1 Entities

#### User Entity
```java
@Entity
@Table(name = "users")
public class User extends AuditableEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String username;

    @Embedded
    private Email email;          // Value Object

    private String password;      // BCrypt hashed
    private String firstName;
    private String lastName;
    private String phoneNumber;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;

    // Business methods
    public void recordSuccessfulLogin() { ... }
    public void recordFailedLogin() { ... }
    public boolean canAuthenticate() { ... }
}
```

#### Product Entity
```java
@Entity
@Table(name = "products")
public class Product extends AuditableEntity {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String description;

    @Column(unique = true)
    private String sku;

    @Embedded
    private Money price;          // Value Object

    private String imageUrl;

    @ManyToOne(fetch = LAZY)
    private Category category;

    private boolean active;

    // Business methods
    public void updatePrice(Money newPrice) { ... }
    public void activate() { ... }
    public void deactivate() { ... }
}
```

#### Order Entity
```java
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String orderNumber;   // ORD-{UUID}

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    private Address shippingAddress;  // Value Object

    private BigDecimal totalAmount;
    private String currency;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    private List<OrderItem> items;

    private LocalDateTime orderedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    // Business methods
    public void confirm() { ... }
    public void ship() { ... }
    public void deliver() { ... }
    public void cancel() { ... }
    public boolean canBeCancelled() { ... }
}
```

### 5.2 Value Objects

Value objects are immutable and compared by value, not identity.

#### Email Value Object
```java
@Embeddable
public class Email {
    private String value;

    public static Email of(String email) {
        // Validates email format
        // Converts to lowercase
        return new Email(email.toLowerCase());
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }
}
```

#### Money Value Object
```java
@Embeddable
public class Money implements Comparable<Money> {
    private BigDecimal amount;
    private CurrencyCode currency;

    public static Money usd(BigDecimal amount) {
        return new Money(amount, CurrencyCode.USD);
    }

    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public String format() {
        return currency.getSymbol() + " " + amount;
    }
}
```

#### Address Value Object
```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s",
            street, city, state, postalCode, country);
    }
}
```

### 5.3 Entity Relationships Diagram

```
┌─────────────────┐
│      USER       │
├─────────────────┤        ┌─────────────────┐
│ id (PK)         │        │   USER_ROLES    │
│ username        │◄───────┤─────────────────┤
│ email           │  1:N   │ user_id (FK)    │
│ password        │        │ role            │
│ firstName       │        └─────────────────┘
│ lastName        │
│ phoneNumber     │
│ enabled         │
│ ...             │
└─────────────────┘


┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    CATEGORY     │       │     PRODUCT     │       │    INVENTORY    │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──────┤ id (PK)         │◄──────┤ id (PK)         │
│ name            │  1:N  │ name            │  1:1  │ product_id (FK) │
│ description     │       │ description     │       │ quantity        │
└─────────────────┘       │ sku             │       │ reservedQuantity│
                          │ price_amount    │       │ reorderLevel    │
                          │ price_currency  │       │ version         │
                          │ imageUrl        │       └─────────────────┘
                          │ category_id(FK) │
                          │ active          │
                          └─────────────────┘


┌─────────────────┐       ┌─────────────────┐
│      ORDER      │       │   ORDER_ITEM    │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │◄──────┤ id (PK)         │
│ orderNumber     │  1:N  │ order_id (FK)   │
│ userId          │       │ productId       │
│ status          │       │ productName     │
│ shipping_*      │       │ quantity        │
│ totalAmount     │       │ unitPrice       │
│ orderedAt       │       └─────────────────┘
│ shippedAt       │
│ deliveredAt     │
└─────────────────┘
```

---

## 6. API Specifications

### 6.1 User Service APIs

#### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/auth/register | Register new user | Public |
| POST | /api/auth/login | User login | Public |
| POST | /api/auth/refresh | Refresh access token | Public |
| GET | /api/auth/validate | Validate JWT token | Public |
| GET | /api/auth/me | Get current user info | Authenticated |

#### User Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/users | Create user | Admin |
| GET | /api/users/{id} | Get user by ID | Authenticated |
| GET | /api/users | List all users (paginated) | Admin |
| PUT | /api/users/{id} | Update user | Owner/Admin |
| DELETE | /api/users/{id} | Delete user | Admin |
| PATCH | /api/users/{id}/enable | Enable user | Admin |
| PATCH | /api/users/{id}/disable | Disable user | Admin |
| PATCH | /api/users/{id}/unlock | Unlock user | Admin |

#### Request/Response Examples

**Login Request**
```json
POST /api/auth/login
{
    "usernameOrEmail": "john@example.com",
    "password": "SecureP@ss123"
}
```

**Login Response**
```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "expiresIn": 86400000,
        "tokenType": "Bearer"
    },
    "message": "Login successful"
}
```

### 6.2 Product Service APIs

#### Product Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/products | Create product | Admin |
| GET | /api/products/{id} | Get product | Public |
| GET | /api/products | List products | Public |
| POST | /api/products/search | Search products | Public |
| PUT | /api/products/{id} | Update product | Admin |
| DELETE | /api/products/{id} | Delete product | Admin |

#### Inventory Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | /api/inventory/product/{id} | Get stock info | Public |
| POST | /api/inventory/reserve | Reserve stock | Service |
| POST | /api/inventory/release | Release reservation | Service |
| POST | /api/inventory/confirm | Confirm reservation | Service |

### 6.3 Order Service APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/orders | Create order | Authenticated |
| GET | /api/orders/{id} | Get order | Owner/Admin |
| GET | /api/orders/user/{userId} | Get user's orders | Owner/Admin |
| POST | /api/orders/{id}/confirm | Confirm order | Admin |
| POST | /api/orders/{id}/cancel | Cancel order | Owner/Admin |
| POST | /api/orders/{id}/ship | Ship order | Admin |
| POST | /api/orders/{id}/deliver | Mark delivered | Admin |

#### Create Order Example

**Request**
```json
POST /api/orders
{
    "userId": 1,
    "items": [
        { "productId": 101, "quantity": 2 },
        { "productId": 102, "quantity": 1 }
    ],
    "shippingAddress": {
        "street": "123 Main St",
        "city": "New York",
        "state": "NY",
        "postalCode": "10001",
        "country": "USA"
    }
}
```

**Response**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "orderNumber": "ORD-a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "status": "PENDING",
        "items": [
            {
                "productId": 101,
                "productName": "Laptop",
                "quantity": 2,
                "unitPrice": 999.99,
                "subtotal": 1999.98
            },
            {
                "productId": 102,
                "productName": "Mouse",
                "quantity": 1,
                "unitPrice": 29.99,
                "subtotal": 29.99
            }
        ],
        "totalAmount": 2029.97,
        "currency": "USD",
        "orderedAt": "2024-01-15T10:30:00"
    }
}
```

---

## 7. Design Patterns

### 7.1 Patterns Overview

| Pattern | Category | Where Used |
|---------|----------|------------|
| Repository | Data Access | All repositories |
| Service Layer | Architecture | All services |
| DTO | Data Transfer | Request/Response objects |
| Value Object | DDD | Email, Money, Address |
| Factory | Creational | Value object creation |
| Strategy | Behavioral | Notification services |
| Observer | Behavioral | Event publishing |
| Circuit Breaker | Resilience | Feign clients |
| Gateway | Integration | API Gateway |
| Registry | Infrastructure | Eureka Server |

### 7.2 Pattern Details

#### Repository Pattern
```java
// Abstracts data access
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailValue(String email);
    boolean existsByUsername(String username);
}
```

#### Service Layer Pattern
```java
// Business logic coordination
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Validate products exist
        // 2. Reserve inventory
        // 3. Calculate total
        // 4. Save order
        // 5. Publish event
    }
}
```

#### Value Object Pattern
```java
// Immutable, equality by value
public class Money {
    private final BigDecimal amount;
    private final CurrencyCode currency;

    // Factory method instead of constructor
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        return new Money(amount, currency);
    }

    // Immutable operations return new instance
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### Strategy Pattern (Notifications)
```java
// Interface
public interface NotificationService {
    void sendNotification(String recipient, String message);
}

// Implementations
@Component
public class EmailNotificationService implements NotificationService { }

@Component
public class SmsNotificationService implements NotificationService { }

// Facade that uses strategies
@Component
public class UserNotificationFacade {
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public void notifyUserRegistered(UserRegisteredEvent event) {
        emailService.sendNotification(event.getEmail(), "Welcome!");
    }
}
```

#### Circuit Breaker Pattern
```java
@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}

@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public ProductDto getProduct(Long id) {
        // Return cached data or throw meaningful exception
        throw new ServiceUnavailableException("Product service unavailable");
    }
}
```

---

## 8. Security Architecture

### 8.1 Authentication Flow

```
┌──────────┐     ┌─────────────┐     ┌──────────────┐     ┌────────────┐
│  Client  │     │ API Gateway │     │ User Service │     │  Database  │
└────┬─────┘     └──────┬──────┘     └──────┬───────┘     └─────┬──────┘
     │                  │                   │                   │
     │  POST /login     │                   │                   │
     │─────────────────>│                   │                   │
     │                  │  Forward request  │                   │
     │                  │──────────────────>│                   │
     │                  │                   │  Verify user      │
     │                  │                   │──────────────────>│
     │                  │                   │  User data        │
     │                  │                   │<──────────────────│
     │                  │                   │                   │
     │                  │  Generate JWT     │                   │
     │                  │<──────────────────│                   │
     │  Return tokens   │                   │                   │
     │<─────────────────│                   │                   │
     │                  │                   │                   │
```

### 8.2 JWT Token Structure

```
Header:
{
    "alg": "HS256",
    "typ": "JWT"
}

Payload:
{
    "sub": "john_doe",           // Subject (username)
    "iss": "MicroMart",          // Issuer
    "type": "access",            // Token type
    "roles": ["ROLE_USER"],      // User roles
    "iat": 1704067200,           // Issued at
    "exp": 1704153600            // Expiration
}

Signature:
HMACSHA256(
    base64UrlEncode(header) + "." + base64UrlEncode(payload),
    secret
)
```

### 8.3 Authorization Matrix

| Endpoint | USER | ADMIN | MODERATOR |
|----------|------|-------|-----------|
| GET /products | Yes | Yes | Yes |
| POST /products | No | Yes | No |
| GET /orders (own) | Yes | Yes | Yes |
| GET /orders (all) | No | Yes | No |
| DELETE /users | No | Yes | No |
| PATCH /users/lock | No | Yes | Yes |

### 8.4 Security Best Practices Implemented

1. **Password Security**
   - BCrypt hashing with salt
   - Minimum password requirements
   - Account locking after failed attempts

2. **Token Security**
   - Short-lived access tokens (24h)
   - Longer refresh tokens (7 days)
   - Secure secret key storage

3. **Input Validation**
   - Jakarta Validation annotations
   - Custom validators
   - SQL injection prevention via JPA

4. **API Security**
   - HTTPS enforcement
   - CORS configuration
   - Rate limiting

---

## 9. Data Architecture

### 9.1 Database Per Service

Each microservice has its own database, ensuring:
- Data isolation
- Independent scaling
- Technology flexibility
- No cross-service joins

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ User Service│     │Product Svc  │     │Order Service│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       ▼                   ▼                   ▼
  ┌─────────┐         ┌─────────┐         ┌─────────┐
  │ user_db │         │product_db│         │order_db │
  │         │         │         │         │         │
  │ • users │         │• products│         │• orders │
  │ • roles │         │• category│         │• items  │
  │         │         │• inventor│         │         │
  └─────────┘         └─────────┘         └─────────┘
```

### 9.2 Data Consistency

#### Eventual Consistency via Events

When an order is placed:
1. Order Service creates order (PENDING)
2. Order Service calls Inventory Service to reserve stock
3. Inventory Service publishes `StockReserved` event
4. Order Service confirms order on success

```
Order Service                  Inventory Service
      │                              │
      │  POST /inventory/reserve     │
      │─────────────────────────────>│
      │                              │ Update inventory
      │                              │ Publish StockReserved
      │      200 OK                  │
      │<─────────────────────────────│
      │                              │
      │ Update order status          │
      │                              │
```

#### Optimistic Locking (Inventory)

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
        // Version auto-incremented by Hibernate
    }
}
```

### 9.3 Indexing Strategy

| Table | Index | Columns | Type |
|-------|-------|---------|------|
| users | idx_user_email | email | UNIQUE |
| users | idx_user_username | username | UNIQUE |
| products | idx_product_sku | sku | UNIQUE |
| products | idx_product_category | category_id | NORMAL |
| orders | idx_order_number | order_number | UNIQUE |
| orders | idx_order_user | user_id | NORMAL |
| orders | idx_order_status | status | NORMAL |
| inventory | idx_inventory_product | product_id | UNIQUE |

---

## 10. Event-Driven Architecture

### 10.1 Event Types

#### Domain Events (Internal)
Published via Spring's `ApplicationEventPublisher`:
- `UserRegisteredEvent`
- `StockReservedEvent`
- `LowStockEvent`

#### Integration Events (External)
Published to Kafka topics:
- `order-events`
- `inventory-events`
- `user-events`

### 10.2 Event Structure

```java
public abstract class BaseEvent {
    private String eventId;        // UUID for idempotency
    private String eventType;      // Event classification
    private String source;         // Source service
    private LocalDateTime timestamp;
    private int version;           // Schema version
    private String correlationId;  // Distributed tracing
}
```

### 10.3 Event Flow Example: Order Creation

```
1. Client → API Gateway → Order Service
   POST /api/orders

2. Order Service → Product Service (Feign)
   GET /api/products/{id}  (get product details)

3. Order Service → Product Service (Feign)
   POST /api/inventory/reserve  (reserve stock)

4. Product Service publishes event
   → Kafka (inventory-events)
   → StockReservedEvent

5. Order Service saves order
   → Database (order_db)

6. Order Service publishes event
   → Kafka (order-events)
   → OrderCreatedEvent

7. Response → Client
   OrderResponse with status PENDING
```

---

## 11. Technology Stack

### 11.1 Core Technologies

| Category | Technology | Version | Purpose |
|----------|------------|---------|---------|
| Language | Java | 24 | Primary language |
| Framework | Spring Boot | 3.4.4 | Application framework |
| Cloud | Spring Cloud | 2024.0.0 | Microservices patterns |
| Build | Maven | 3.6+ | Dependency management |

### 11.2 Spring Components

| Component | Dependency | Usage |
|-----------|------------|-------|
| Web | spring-boot-starter-web | REST APIs |
| Data | spring-boot-starter-data-jpa | Database access |
| Security | spring-boot-starter-security | Authentication |
| Validation | spring-boot-starter-validation | Input validation |
| Actuator | spring-boot-starter-actuator | Health checks |

### 11.3 Infrastructure

| Component | Technology | Purpose |
|-----------|------------|---------|
| Database | PostgreSQL | Data persistence |
| Message Broker | Apache Kafka | Event streaming |
| Service Discovery | Netflix Eureka | Service registry |
| API Gateway | Spring Cloud Gateway | Request routing |
| Object Storage | AWS S3 | Image storage |
| Caching | Redis | Session/data caching |

### 11.4 Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| JJWT | 0.12.5 | JWT handling |
| MapStruct | 1.5.5 | DTO mapping |
| Lombok | 1.18.40 | Boilerplate reduction |
| SpringDoc | 2.4.0 | API documentation |
| Resilience4j | - | Circuit breaker |

---

## 12. Deployment Architecture

### 12.1 Container Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      KUBERNETES CLUSTER                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ api-gateway │  │ api-gateway │  │ api-gateway │   Replicas   │
│  │   Pod #1    │  │   Pod #2    │  │   Pod #3    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│         │                │                │                      │
│         └────────────────┼────────────────┘                      │
│                          │                                       │
│                    ┌─────┴─────┐                                 │
│                    │  Service  │                                 │
│                    │   (LB)    │                                 │
│                    └───────────┘                                 │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    MICROSERVICES                         │    │
│  │                                                          │    │
│  │  ┌──────────┐   ┌──────────┐   ┌──────────┐            │    │
│  │  │user-svc  │   │product-  │   │order-svc │            │    │
│  │  │ x3 pods  │   │svc x3    │   │ x3 pods  │            │    │
│  │  └──────────┘   └──────────┘   └──────────┘            │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    DATA LAYER                            │    │
│  │                                                          │    │
│  │  ┌──────────┐   ┌──────────┐   ┌──────────┐            │    │
│  │  │PostgreSQL│   │PostgreSQL│   │PostgreSQL│            │    │
│  │  │ user_db  │   │product_db│   │ order_db │            │    │
│  │  └──────────┘   └──────────┘   └──────────┘            │    │
│  │                                                          │    │
│  │  ┌──────────┐   ┌──────────┐                            │    │
│  │  │  Kafka   │   │  Redis   │                            │    │
│  │  │ Cluster  │   │ Cluster  │                            │    │
│  │  └──────────┘   └──────────┘                            │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 12.2 CI/CD Pipeline

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  Code   │───>│  Build  │───>│  Test   │───>│  Docker │───>│ Deploy  │
│  Push   │    │ (Maven) │    │ (JUnit) │    │  Build  │    │  (K8s)  │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
     │              │              │              │              │
     │              │              │              │              │
     ▼              ▼              ▼              ▼              ▼
  GitHub        Compile       Unit Tests    Container      Kubernetes
  Actions       & Package     Integration   Registry       Cluster
```

### 12.3 Environment Configuration

| Environment | Purpose | Configuration |
|-------------|---------|---------------|
| local | Development | H2/LocalStack |
| dev | Integration testing | Shared services |
| staging | Pre-production | Production-like |
| prod | Production | Full HA setup |

---

## Appendix A: Quick Reference

### Service Ports

| Service | Port | Context Path |
|---------|------|--------------|
| API Gateway | 8080 | / |
| User Service | 8081 | /api |
| Product Service | 8082 | /api |
| Order Service | 8083 | /api |
| Eureka Server | 8761 | / |

### Kafka Topics

| Topic | Producer | Consumer |
|-------|----------|----------|
| order-events | Order Service | Product Service |
| inventory-events | Product Service | Order Service |
| user-events | User Service | - |

### Common Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (duplicate) |
| 500 | Server Error |

---

*Document Version: 1.0*
*Last Updated: January 2026*
