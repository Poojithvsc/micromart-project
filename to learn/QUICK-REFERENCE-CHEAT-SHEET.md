# MicroMart Quick Reference Cheat Sheet

## Service Ports & URLs

| Service | Port | Base URL | Health Check |
|---------|------|----------|--------------|
| API Gateway | 8080 | http://localhost:8080 | /actuator/health |
| Eureka Server | 8761 | http://localhost:8761 | /actuator/health |
| User Service | 8081 | http://localhost:8081/api | /actuator/health |
| Product Service | 8082 | http://localhost:8082/api | /actuator/health |
| Order Service | 8083 | http://localhost:8083/api | /actuator/health |

---

## Essential API Endpoints

### Authentication (via Gateway: localhost:8080)

```bash
# Register new user
POST /api/auth/register
Body: { "username": "john", "email": "john@email.com", "password": "Pass123!", "firstName": "John", "lastName": "Doe" }

# Login
POST /api/auth/login
Body: { "usernameOrEmail": "john@email.com", "password": "Pass123!" }

# Use token in subsequent requests
Header: Authorization: Bearer <access_token>
```

### Products

```bash
# Get all products
GET /api/products

# Get product by ID
GET /api/products/1

# Search products
POST /api/products/search
Body: { "name": "laptop", "minPrice": 100, "maxPrice": 2000 }

# Create product (Admin)
POST /api/products
Body: { "name": "Laptop", "sku": "LAP-001", "price": 999.99, "categoryId": 1 }
```

### Orders

```bash
# Create order
POST /api/orders
Body: {
  "userId": 1,
  "items": [{ "productId": 1, "quantity": 2 }],
  "shippingAddress": { "street": "123 Main", "city": "NYC", "state": "NY", "postalCode": "10001", "country": "USA" }
}

# Get order
GET /api/orders/1

# Cancel order
POST /api/orders/1/cancel
```

---

## Project Structure

```
micromart/
├── api-gateway/          # Port 8080 - Routes all requests
├── eureka-server/        # Port 8761 - Service discovery
├── common/               # Shared code (DTOs, exceptions)
├── user-service/         # Port 8081 - Auth & users
├── product-service/      # Port 8082 - Products & inventory
└── order-service/        # Port 8083 - Orders
```

### Each Service Structure

```
service/
├── controller/           # @RestController - HTTP endpoints
├── service/              # @Service - Business logic
├── repository/           # @Repository - Database access
├── domain/               # @Entity - JPA entities
│   └── valueobject/      # @Embeddable - Value objects
├── dto/
│   ├── request/          # Input DTOs
│   └── response/         # Output DTOs
├── mapper/               # Entity <-> DTO converters
├── config/               # Configuration classes
└── event/                # Domain events
```

---

## Key Classes by Service

### User Service
| Layer | Key Classes |
|-------|-------------|
| Controller | `UserController`, `AuthController` |
| Service | `UserServiceImpl`, `AuthServiceImpl` |
| Repository | `UserRepository` |
| Entity | `User`, `Role` (enum) |
| Value Object | `Email` |
| Security | `JwtTokenProvider`, `JwtAuthenticationFilter` |

### Product Service
| Layer | Key Classes |
|-------|-------------|
| Controller | `ProductController`, `CategoryController`, `InventoryController` |
| Service | `ProductServiceImpl`, `CategoryServiceImpl`, `InventoryServiceImpl` |
| Repository | `ProductRepository`, `CategoryRepository`, `InventoryRepository` |
| Entity | `Product`, `Category`, `Inventory` |
| Value Object | `Money` |
| External | `S3StorageService` |

### Order Service
| Layer | Key Classes |
|-------|-------------|
| Controller | `OrderController` |
| Service | `OrderServiceImpl` |
| Repository | `OrderRepository` |
| Entity | `Order`, `OrderItem`, `OrderStatus` (enum) |
| Value Object | `Address` |
| Clients | `ProductClient`, `InventoryClient` (Feign) |

---

## Common Commands

### Build & Run

```bash
# Build all services
mvn clean install

# Run specific service
cd user-service && mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Skip tests
mvn install -DskipTests
```

### Docker

```bash
# Build image
docker build -t micromart/user-service .

# Run with docker-compose
docker-compose up -d
```

### Database

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d user_db

# Common queries
SELECT * FROM users;
SELECT * FROM products WHERE active = true;
SELECT * FROM orders WHERE status = 'PENDING';
```

---

## Response Format

All APIs return responses in this format:

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response

```json
{
  "success": false,
  "data": null,
  "message": "User not found with id: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## HTTP Status Codes Used

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation failed |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | No permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Server Error | Unexpected error |

---

## JWT Token Structure

```
Header.Payload.Signature

Payload contains:
- sub: username
- roles: ["ROLE_USER", "ROLE_ADMIN"]
- iss: "MicroMart"
- iat: issued timestamp
- exp: expiration timestamp
```

---

## Kafka Topics

| Topic | Producer | Consumer | Events |
|-------|----------|----------|--------|
| user-events | User Service | - | UserRegistered, UserLoggedIn |
| inventory-events | Product Service | Order Service | StockReserved, LowStock |
| order-events | Order Service | Product Service | OrderCreated, OrderShipped |

---

## Key Annotations Quick Reference

| Annotation | Purpose | Layer |
|------------|---------|-------|
| `@RestController` | REST API endpoint | Controller |
| `@Service` | Business logic | Service |
| `@Repository` | Database access | Repository |
| `@Entity` | JPA table mapping | Domain |
| `@Transactional` | Transaction boundary | Service |
| `@Valid` | Trigger validation | Controller |
| `@PreAuthorize` | Security check | Controller/Service |
| `@FeignClient` | HTTP client | Client |

---

## Order Lifecycle

```
PENDING --> CONFIRMED --> PAYMENT_COMPLETED --> PROCESSING --> SHIPPED --> DELIVERED
    |           |
    v           v
 CANCELLED   CANCELLED (before shipping)
```

---

## Troubleshooting

### Service won't start
1. Check if port is already in use: `netstat -an | grep <port>`
2. Check database connection in application.yml
3. Check Eureka is running (for dependent services)

### 401 Unauthorized
1. Token expired? Get new token via /auth/login
2. Token missing? Add `Authorization: Bearer <token>` header
3. Wrong role? Check endpoint requires which role

### 404 Not Found
1. Check service is running
2. Check URL path is correct
3. Check resource exists in database

### Connection refused
1. Check target service is running
2. Check Eureka registration
3. Check network/firewall

---

## Useful Links

- **Eureka Dashboard**: http://localhost:8761
- **Swagger UI** (per service): http://localhost:808X/swagger-ui.html
- **Actuator**: http://localhost:808X/actuator
- **Health**: http://localhost:808X/actuator/health

---

*Keep this cheat sheet handy for quick reference!*
