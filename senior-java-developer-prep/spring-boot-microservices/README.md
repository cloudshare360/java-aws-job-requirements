# Spring Boot Microservices

## Overview
This section covers Spring Boot framework concepts essential for building microservices in financial services environments.

---

## Spring Boot Configuration

### Application Profiles

#### Setting Active Profiles

**1. application.properties**
```properties
spring.profiles.active=dev
```

**2. Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

**3. Command Line**
```bash
java -jar app.jar --spring.profiles.active=prod
```

**4. Programmatically**
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
```

#### Profile-Specific Configuration Files
- `application.properties` - Default configuration
- `application-dev.properties` - Development environment
- `application-test.properties` - Testing environment
- `application-prod.properties` - Production environment

### Environment Properties

#### Property Sources (Priority Order - Highest to Lowest)
1. Command line arguments
2. Java System properties (`System.getProperties()`)
3. OS environment variables
4. `application-{profile}.properties` outside jar
5. `application-{profile}.properties` inside jar
6. `application.properties` outside jar
7. `application.properties` inside jar

#### Example Configuration
```properties
# application.properties
server.port=8080
spring.application.name=payment-service

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/findb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

---

## Spring Annotations

### JPA Annotations

#### @Entity
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
```

### Configuration Annotations

#### @Configuration
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        // Production datasource configuration
    }
}
```

### Component Annotations
- `@RestController` - REST API endpoints
- `@Service` - Business logic layer
- `@Repository` - Data access layer
- `@Component` - Generic Spring-managed component

---

## Spring Security

### Basic Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt();
    }
}
```

### JWT Token Implementation

```java
@Service
public class JwtTokenService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
}
```

---

## Interceptors & Filters

### HandlerInterceptor

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler, 
                          ModelAndView modelAndView) {
        log.info("Response Status: {}", response.getStatus());
    }
}
```

### Filter

```java
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = extractToken(request);
        if (token != null && validateToken(token)) {
            // Set authentication in SecurityContext
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Key Differences
| Aspect | Filter | Interceptor |
|--------|--------|-------------|
| Part of | Servlet API | Spring MVC |
| Access to | Request/Response | Handler, ModelAndView |
| Use Case | Security, logging | Business logic, model manipulation |
| Execution | Before/after servlet | Before/after controller |

---

## Microservices Patterns

### Service Communication
- REST APIs
- Message queues (RabbitMQ, Kafka)
- gRPC for internal services

### Configuration Management
- Spring Cloud Config Server
- External configuration repositories
- Encrypted properties

### Service Discovery
- Eureka
- Consul
- Kubernetes service discovery

### Circuit Breaker
```java
@Service
public class PaymentService {
    
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
    public PaymentResponse processPayment(PaymentRequest request) {
        // Call external payment gateway
    }
    
    public PaymentResponse fallbackPayment(PaymentRequest request, Exception ex) {
        // Fallback logic
        return PaymentResponse.failed("Service temporarily unavailable");
    }
}
```

---

## Best Practices for Financial Services

1. **Transaction Management**: Use `@Transactional` appropriately
2. **Audit Logging**: Track all financial operations
3. **Idempotency**: Ensure payment operations are idempotent
4. **Data Validation**: Strict input validation for financial data
5. **Error Handling**: Comprehensive exception handling
6. **Testing**: Unit, integration, and contract testing
7. **Monitoring**: Health checks, metrics, and alerting
