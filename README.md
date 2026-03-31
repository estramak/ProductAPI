# Product API

A RESTful API for managing products, built with Spring Boot 4. Supports full CRUD operations with basic authentication and global exception handling.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17+ (21 or 25 recommended) |
| Spring Boot | 4.0.5 |
| Spring Security | 7.0.4 |
| Spring Framework | 7 |
| Spring Data JPA | managed by Boot BOM |
| H2 Database | managed by Boot BOM |
| Maven | 3.9+ |

---

## Project Structure

```
src/
├── main/
│   └── java/com/example/productapi/
│       ├── config/
│       │   └── SecurityConfig.java         # Basic auth, BCrypt password encoding
│       ├── controller/
│       │   └── ProductController.java      # REST endpoints
│       ├── exception/
│       │   ├── ErrorDetails.java           # Error response body
│       │   ├── GlobalExceptionHandler.java # @ControllerAdvice handler
│       │   └── ResourceNotFoundException.java
│       ├── model/
│       │   └── Product.java                # JPA entity (id, name, price)
│       ├── repository/
│       │   └── ProductRepository.java      # JpaRepository interface
│       └── service/
│           └── ProductService.java         # Business logic / CRUD operations
└── test/
    └── java/com/example/productapi/
        ├── controller/
        │   └── ProductControllerTest.java  # @WebMvcTest slice tests
        └── service/
            └── ProductServiceTest.java     # Mockito unit tests
```

---

## Getting Started

### Prerequisites

- JDK 17 or higher ([download](https://adoptium.net/))
- Maven 3.9+

### Run locally

```bash
# Clone the repository
git clone <repo-url>
cd productapi

# Build and run
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

### Build a JAR

```bash
mvn clean package
java -jar target/productapi-0.0.1-SNAPSHOT.jar
```

---

## Authentication

All endpoints require **HTTP Basic Authentication**.

| Field | Value |
|---|---|
| Username | `user` |
| Password | `password` |

> **Note:** Credentials are configured in `SecurityConfig.java` using an in-memory user store with BCrypt password hashing. This is suitable for development — replace with a persistent user store for production.

Example with curl:
```bash
curl -u user:password http://localhost:8080/api/products
```

---

## API Endpoints

Base URL: `/api/products`

### Get all products

```
GET /api/products
```

**Response:** `200 OK`
```json
[
  { "id": 1, "name": "Widget", "price": 9.99 },
  { "id": 2, "name": "Gadget", "price": 24.99 }
]
```

---

### Get product by ID

```
GET /api/products/{id}
```

**Response:** `200 OK`
```json
{ "id": 1, "name": "Widget", "price": 9.99 }
```

**Error:** `404 Not Found` if the product does not exist
```json
{
  "message": "Product not found",
  "details": "uri=/api/products/99"
}
```

---

### Create a product

```
POST /api/products
Content-Type: application/json
```

**Request body:**
```json
{ "name": "Widget", "price": 9.99 }
```

**Response:** `201 Created`
```json
{ "id": 1, "name": "Widget", "price": 9.99 }
```

---

### Update a product

```
PUT /api/products/{id}
Content-Type: application/json
```

**Request body:**
```json
{ "name": "Updated Widget", "price": 12.99 }
```

**Response:** `200 OK`
```json
{ "id": 1, "name": "Updated Widget", "price": 12.99 }
```

**Error:** `404 Not Found` if the product does not exist

---

### Delete a product

```
DELETE /api/products/{id}
```

**Response:** `204 No Content`

**Error:** `404 Not Found` if the product does not exist

---

## Error Handling

All errors are returned in a consistent format via `GlobalExceptionHandler`:

```json
{
  "message": "<error description>",
  "details": "<request URI>"
}
```

| Scenario | Status |
|---|---|
| Product not found | `404 Not Found` |
| Unexpected server error | `500 Internal Server Error` |

---

## Running Tests

```bash
mvn test
```

The test suite includes:

- **`ProductServiceTest`** — unit tests for the service layer using Mockito, no Spring context loaded
- **`ProductControllerTest`** — web layer slice tests using `@WebMvcTest` and `MockMvc`, only the controller and exception handler are loaded

---

## Database

The project uses an **H2 in-memory database** configured automatically by Spring Boot. Data is reset on every restart.

The H2 console is available at `http://localhost:8080/h2-console` if enabled in `application.properties`:

```properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
```

---

## Notes

- Built against **Spring Boot 4.0.5** / **Spring Security 7.0.4** — see [CHANGELOG.md](Changelog.md) for a full list of deviations from the tutorial this project was based on
- `jakarta.*` packages are used throughout (not `javax.*`), as required by Jakarta EE 11
- CSRF protection is disabled to allow stateless REST calls; re-enable for browser-facing applications
