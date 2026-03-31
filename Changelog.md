# Spring Boot REST API — Change Log

> Deviations from the [index.dev tutorial](https://www.index.dev/blog/build-rest-api-java-spring-boot) and rationale  
> **Project versions: Spring Boot 4.0.5 · Spring Security 7.0.4 · Spring Framework 7**

---

## Overview

This document records every deliberate change made to the tutorial's source code with the original snippet, the updated code, and the reason for each change. Changes fall into four categories:

- **Spring Boot 4.x API updates** — APIs removed or relocated in the Boot 3→4 major version jump
- **Spring Security 7 API updates** — chained DSL and deprecated utilities fully removed since Security 6
- **Dependency injection best practices** — constructor injection over `@Autowired` field injection
- **HTTP semantics** — correct status codes for REST operations
- **Test strategy** — focused `@WebMvcTest` slice over full `@SpringBootTest` integration tests

---

## Change Summary

| # | File | Change | Category |
|---|------|---------|----------|
| 1 | `ProductController.java` | Constructor injection replaces `@Autowired` | Best practice |
| 2 | `ProductController.java` | `createProduct()` returns `201 Created` | HTTP semantics |
| 3 | `ProductService.java` | Added `ResourceNotFoundException` import | Compilation fix |
| 4 | `SecurityConfig.java` | `@Configuration` annotation added | Spring Boot 4.x |
| 5 | `SecurityConfig.java` | Lambda-style security DSL | Spring Security 7 |
| 6 | `SecurityConfig.java` | `BCryptPasswordEncoder` bean; `withDefaultPasswordEncoder()` removed | Spring Security 7 |
| 7 | `GlobalExceptionHandler.java` | Method renamed `globalExceptionsHandler` | Naming |
| 8 | `ProductControllerTest.java` | `@SpringBootTest` replaced with `@WebMvcTest` | Test strategy |
| 9 | `ProductControllerTest.java` | `@WebMvcTest` import path updated | Spring Boot 4.x |
| 10 | `ProductControllerTest.java` | `@MockBean` replaced with `@MockitoBean` | Spring Boot 4.x |

---

## Detailed Changes

---

### 1. `ProductController.java` — Constructor injection replaces `@Autowired`

**Tutorial code:**
```java
@Autowired
private ProductService productService;
```

**Updated code:**
```java
private final ProductService productService;

public ProductController(ProductService productService) {
    this.productService = productService;
}
```

**Reason:** Constructor injection is the recommended Spring best practice. It makes dependencies explicit and allows the class to be unit tested without a Spring context. `@Autowired` field injection is discouraged in the official Spring documentation.

---

### 2. `ProductController.java` — `createProduct()` returns `201 Created`

**Tutorial code:**
```java
@PostMapping
public Product createProduct(@RequestBody Product product) {
    return productService.createProduct(product);
}
```

**Updated code:**
```java
@PostMapping
public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    Product created = productService.createProduct(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

**Reason:** REST conventions require POST endpoints to return HTTP `201 Created` rather than `200 OK`. Wrapping in `ResponseEntity` gives full control over the status code and aligns with the pattern already used by the other endpoints in the controller.

---

### 3. `ProductService.java` — Added `ResourceNotFoundException` import

**Tutorial code:**
```java
// No explicit import — tutorial assumes same package or omits it
```

**Updated code:**
```java
import com.example.productapi.exception.ResourceNotFoundException;
```

**Reason:** The tutorial references `ResourceNotFoundException` in `updateProduct()` and `deleteProduct()` without importing it. The explicit import is required for the code to compile given the `productapi` package structure used in this project.

---

### 4. `SecurityConfig.java` — `@Configuration` annotation added

**Tutorial code:**
```java
@EnableWebSecurity
public class SecurityConfig { ... }
```

**Updated code:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig { ... }
```

**Reason:** In Spring Boot 3+ (and 4.x), `@EnableWebSecurity` alone does not register the class as a Spring-managed bean. `@Configuration` is required so the `@Bean` methods (`securityFilterChain`, `userDetailsService`, `passwordEncoder`) are picked up by the application context.

---

### 5. `SecurityConfig.java` — Lambda-style security DSL

**Tutorial code:**
```java
http.csrf().disable()
    .authorizeRequests()
    .anyRequest().authenticated()
    .and()
    .httpBasic();
```

**Updated code:**
```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth
        .anyRequest().authenticated())
    .httpBasic(Customizer.withDefaults());
```

**Reason:** The chained API (`csrf().disable()`, `authorizeRequests()`, `.and()`) was deprecated in Spring Security 6 and **fully removed in Spring Security 7.0** (which ships with Spring Boot 4.x). This project uses **Spring Security 7.0.4**, so the old style will not compile. The lambda DSL is the only supported style from Security 6 onwards.

---

### 6. `SecurityConfig.java` — `BCryptPasswordEncoder` bean; `withDefaultPasswordEncoder()` removed

**Tutorial code:**
```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("user")
        .password("password")
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
}
```

**Updated code:**
```java
@Bean
public UserDetailsService userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.builder()
        .username("user")
        .password(encoder.encode("password"))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Reason:** `User.withDefaultPasswordEncoder()` was deprecated in Spring Security 5 and **fully removed in Spring Security 7.0**. On this project's **Spring Security 7.0.4**, the tutorial code will not compile. A dedicated `BCryptPasswordEncoder` bean is the correct replacement. Passwords are properly hashed before storage and the encoder is reusable across the application.

---

### 7. `GlobalExceptionHandler.java` — Method renamed `globalExceptionsHandler`

**Tutorial code:**
```java
public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) { ... }
```

**Updated code:**
```java
public ResponseEntity<?> globalExceptionsHandler(Exception ex, WebRequest request) { ... }
```

**Reason:** Minor rename for consistency with the plural naming convention used elsewhere in the project. No functional impact.

---

### 8. `ProductControllerTest.java` — `@SpringBootTest` replaced with `@WebMvcTest`

**Tutorial code:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ProductRepository productRepository;
    // tests all products via live HTTP
}
```

**Updated code:**
```java
@WebMvcTest({ProductController.class, GlobalExceptionHandler.class})
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;
    // tests controller HTTP behaviour via MockMvc
}
```

**Reason:** `@WebMvcTest` is faster and more focused. It loads only the web layer (controller + exception handler) and mocks the service, avoiding full application context startup and database setup. This makes it a true unit test of the controller's HTTP behaviour. Note: `TestRestTemplate` is also being steered toward deprecation in Boot 4.x in favour of `RestTestClient`, making the `MockMvc` approach the more future-proof choice.

---

### 9. `ProductControllerTest.java` — `@WebMvcTest` import path updated

**Tutorial code:**
```java
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
```

**Updated code:**
```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
```

**Reason:** Spring Boot 4.0 restructured its autoconfiguration into smaller focused modules. `@WebMvcTest` was relocated to the `spring-boot-webmvc-test` module. The old import path no longer exists in Spring Boot 4.x and will cause a compilation error.

---

### 10. `ProductControllerTest.java` — `@MockBean` replaced with `@MockitoBean`

**Tutorial code:**
```java
import org.springframework.boot.test.mock.mockito.MockBean;

@MockBean
private ProductService productService;
```

**Updated code:**
```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@MockitoBean
private ProductService productService;
```

**Reason:** `@MockBean` was deprecated in Spring Boot 3.4 and **fully removed in Spring Boot 4.0**. `@MockitoBean`, provided by the `spring-test` module, is the current standard for replacing beans with Mockito mocks in Spring test slices. Using `@MockBean` in this project will cause a compilation error.

---

*Generated against Spring Boot 4.0.5 · Spring Security 7.0.4 · Spring Framework 7*
