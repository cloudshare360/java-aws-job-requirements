# 🚀 Quick Reference Cheat Sheet

**Last-minute review guide for Senior Java Developer interviews**

---

## 📑 Table of Contents

- [Spring Boot](#spring-boot)
- [Spring Security](#spring-security)
- [REST API](#rest-api)
- [Java Streams](#java-streams)
- [Kubernetes](#kubernetes)
- [Jenkins](#jenkins)
- [Banking Concepts](#banking-concepts)

---

## Spring Boot

### Configuration & Profiles

```properties
# application.properties
spring.profiles.active=prod
server.port=8080
spring.application.name=payment-service

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bankdb
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

### Profile Activation Methods
```bash
# 1. Properties file
spring.profiles.active=prod

# 2. Environment variable
export SPRING_PROFILES_ACTIVE=prod

# 3. Command line
java -jar app.jar --spring.profiles.active=prod

# 4. Programmatically
SpringApplication.setAdditionalProfiles("prod")
```

### Common Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@SpringBootApplication` | Main application class | Entry point |
| `@RestController` | REST endpoints | `@RestController` + `@ResponseBody` |
| `@Service` | Business logic | Service layer |
| `@Repository` | Data access | DAO layer |
| `@Configuration` | Bean configuration | Config classes |
| `@Component` | Generic component | Auto-detected beans |
| `@Autowired` | Dependency injection | Constructor/field injection |
| `@Value` | Property injection | `@Value("${api.key}")` |
| `@Profile` | Profile-specific beans | `@Profile("prod")` |

### JPA Annotations

```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String accountNumber;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal balance;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```

### Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## Spring Security

### Basic Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JWT Token Generation

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", userDetails.getAuthorities());
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

### Interceptor vs Filter

| Aspect | Filter | Interceptor |
|--------|--------|-------------|
| **API** | Servlet API | Spring MVC |
| **Execution** | Before/after servlet | Before/after controller |
| **Access to** | Request/Response | Handler, ModelAndView |
| **Use case** | Security, logging | Business logic, validation |
| **Order** | Executes first | Executes after filters |

```java
// Filter
@Component
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // Before request
        chain.doFilter(request, response);
        // After response
    }
}

// Interceptor
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Before controller
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // After controller, before view
    }
}
```

---

## REST API

### HTTP Methods

| Method | Purpose | Idempotent | Safe | Example |
|--------|---------|------------|------|---------|
| **GET** | Retrieve resource | ✅ | ✅ | Get account details |
| **POST** | Create resource | ❌ | ❌ | Create new account |
| **PUT** | Update/Replace | ✅ | ❌ | Update account info |
| **PATCH** | Partial update | ❌ | ❌ | Update balance only |
| **DELETE** | Delete resource | ✅ | ❌ | Close account |

### Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| **200** | OK | Successful GET, PUT, PATCH |
| **201** | Created | Successful POST |
| **204** | No Content | Successful DELETE |
| **400** | Bad Request | Invalid input |
| **401** | Unauthorized | Missing/invalid auth |
| **403** | Forbidden | Insufficient permissions |
| **404** | Not Found | Resource doesn't exist |
| **409** | Conflict | Duplicate resource |
| **500** | Server Error | Internal error |

### POST vs PUT

```java
// POST - Create new resource (non-idempotent)
@PostMapping("/accounts")
public ResponseEntity<Account> createAccount(@RequestBody AccountRequest request) {
    Account account = accountService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(account);
}

// PUT - Update/Replace resource (idempotent)
@PutMapping("/accounts/{id}")
public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody AccountRequest request) {
    Account account = accountService.update(id, request);
    return ResponseEntity.ok(account);
}
```

### API Best Practices

```java
// ✅ Good: RESTful naming
GET    /api/v1/accounts
POST   /api/v1/accounts
GET    /api/v1/accounts/{id}
PUT    /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}

// ❌ Bad: Non-RESTful
GET    /api/v1/getAccount?id=123
POST   /api/v1/createAccount
POST   /api/v1/updateAccount
```

---

## Java Streams

### Common Operations

```java
List<Transaction> transactions = getTransactions();

// Filter
List<Transaction> highValue = transactions.stream()
    .filter(t -> t.getAmount().compareTo(new BigDecimal("10000")) > 0)
    .collect(Collectors.toList());

// Map
List<String> accountNumbers = transactions.stream()
    .map(Transaction::getAccountNumber)
    .collect(Collectors.toList());

// FlatMap
List<String> allTags = transactions.stream()
    .flatMap(t -> t.getTags().stream())
    .collect(Collectors.toList());

// Reduce
BigDecimal total = transactions.stream()
    .map(Transaction::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// GroupBy
Map<String, List<Transaction>> byType = transactions.stream()
    .collect(Collectors.groupingBy(Transaction::getType));

// Count
long count = transactions.stream()
    .filter(t -> t.getStatus() == Status.COMPLETED)
    .count();

// Distinct
List<String> uniqueTypes = transactions.stream()
    .map(Transaction::getType)
    .distinct()
    .collect(Collectors.toList());

// Sorted
List<Transaction> sorted = transactions.stream()
    .sorted(Comparator.comparing(Transaction::getAmount).reversed())
    .collect(Collectors.toList());

// Limit & Skip
List<Transaction> page = transactions.stream()
    .skip(20)  // Skip first 20
    .limit(10) // Take next 10
    .collect(Collectors.toList());
```

### Banking Examples

```java
// Find suspicious transactions
List<Transaction> suspicious = transactions.stream()
    .filter(t -> t.getAmount().compareTo(new BigDecimal("10000")) > 0)
    .filter(t -> t.isInternational())
    .filter(t -> t.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5)))
    .collect(Collectors.toList());

// Calculate total balance by account type
Map<AccountType, BigDecimal> balanceByType = accounts.stream()
    .collect(Collectors.groupingBy(
        Account::getType,
        Collectors.mapping(
            Account::getBalance,
            Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
        )
    ));

// Find top 10 customers by transaction volume
List<Customer> topCustomers = customers.stream()
    .sorted(Comparator.comparing(Customer::getTransactionCount).reversed())
    .limit(10)
    .collect(Collectors.toList());
```

### Collectors Cheat Sheet

```java
// To List
Collectors.toList()

// To Set
Collectors.toSet()

// To Map
Collectors.toMap(keyMapper, valueMapper)

// GroupBy
Collectors.groupingBy(classifier)

// Counting
Collectors.counting()

// Summing
Collectors.summingInt(mapper)
Collectors.summingLong(mapper)
Collectors.summingDouble(mapper)

// Joining
Collectors.joining(", ")

// Partitioning
Collectors.partitioningBy(predicate)
```

---

## Kubernetes

### Essential Commands

```bash
# Pods
kubectl get pods
kubectl get pods -n namespace
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl logs <pod-name> -f  # Follow logs
kubectl exec -it <pod-name> -- /bin/bash
kubectl delete pod <pod-name>

# Deployments
kubectl get deployments
kubectl describe deployment <name>
kubectl scale deployment <name> --replicas=5
kubectl rollout status deployment <name>
kubectl rollout undo deployment <name>

# Services
kubectl get services
kubectl describe service <name>
kubectl expose deployment <name> --port=8080 --type=LoadBalancer

# ConfigMaps & Secrets
kubectl create configmap <name> --from-file=config.properties
kubectl create secret generic <name> --from-literal=password=secret123
kubectl get configmaps
kubectl get secrets

# Namespaces
kubectl get namespaces
kubectl create namespace <name>
kubectl config set-context --current --namespace=<name>

# Resource Usage
kubectl top nodes
kubectl top pods
```

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  labels:
    app: payment-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
      - name: payment-service
        image: payment-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

### Service YAML

```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
spec:
  selector:
    app: payment-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Resource Limits

| Resource | Request | Limit | Meaning |
|----------|---------|-------|---------|
| **CPU** | 250m | 500m | 0.25 to 0.5 cores |
| **Memory** | 256Mi | 512Mi | 256MB to 512MB |

**Request**: Guaranteed resources  
**Limit**: Maximum allowed resources

### Health Checks

```java
// Spring Boot Actuator
@RestController
public class HealthController {
    
    @GetMapping("/actuator/health/liveness")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok("UP");
    }
    
    @GetMapping("/actuator/health/readiness")
    public ResponseEntity<String> readiness() {
        // Check database connection, external services, etc.
        if (isDatabaseConnected() && areExternalServicesUp()) {
            return ResponseEntity.ok("UP");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DOWN");
    }
}
```

---

## Jenkins

### Declarative Pipeline

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_NAME = 'payment-service'
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/org/repo.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                sh 'mvn sonar:sonar'
            }
        }
        
        stage('Security Scan') {
            steps {
                sh 'mvn dependency-check:check'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh """
                    docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} .
                    docker tag ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                """
            }
        }
        
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh """
                        echo \$PASS | docker login -u \$USER --password-stdin
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }
        
        stage('Deploy to K8s') {
            steps {
                sh """
                    kubectl set image deployment/payment-service payment-service=${DOCKER_REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
                    kubectl rollout status deployment/payment-service
                """
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

---

## Banking Concepts

### BigDecimal for Money

```java
// ✅ Correct: Use BigDecimal
BigDecimal balance = new BigDecimal("1000.00");
BigDecimal amount = new BigDecimal("50.25");
BigDecimal newBalance = balance.subtract(amount);

// ❌ Wrong: Never use float/double for money
double balance = 1000.00;
double amount = 50.25;
double newBalance = balance - amount; // Precision issues!

// Rounding
BigDecimal result = amount.setScale(2, RoundingMode.HALF_UP);

// Comparison
if (balance.compareTo(amount) > 0) {
    // balance > amount
}
```

### Transaction Idempotency

```java
@Service
public class PaymentService {
    
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Check if already processed (idempotency)
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return PaymentResponse.from(existing.get());
        }
        
        // Process payment
        Payment payment = new Payment();
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.COMPLETED);
        
        paymentRepository.save(payment);
        
        return PaymentResponse.from(payment);
    }
}
```

### Audit Logging

```java
@Aspect
@Component
public class AuditAspect {
    
    @Around("@annotation(Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Log before
        auditLog.info("Method: {}, Args: {}", method, args);
        
        Object result = joinPoint.proceed();
        
        // Log after
        auditLog.info("Method: {}, Result: {}", method, result);
        
        return result;
    }
}

// Usage
@Auditable
public void transferFunds(String from, String to, BigDecimal amount) {
    // Transfer logic
}
```

### Compliance Requirements

| Regulation | Requirement | Implementation |
|------------|-------------|----------------|
| **PCI-DSS** | Encrypt card data | AES-256, TLS 1.3 |
| **SOX** | Audit trails | Log all transactions |
| **GLBA** | Customer privacy | Data encryption, access control |
| **KYC/AML** | Identity verification | Document verification, monitoring |

### High Availability Patterns

```java
// Circuit Breaker
@CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallbackPayment")
public PaymentResponse processPayment(PaymentRequest request) {
    return externalGateway.process(request);
}

public PaymentResponse fallbackPayment(PaymentRequest request, Exception ex) {
    return PaymentResponse.error("Service temporarily unavailable");
}

// Retry
@Retry(name = "accountService", maxAttempts = 3)
public Account getAccount(String accountNumber) {
    return accountService.findByNumber(accountNumber);
}

// Rate Limiter
@RateLimiter(name = "api", fallbackMethod = "rateLimitFallback")
public ResponseEntity<?> handleRequest() {
    // Process request
}
```

---

## 🎯 Interview Tips

### Technical Questions
1. **Clarify first**: Ask about requirements, constraints
2. **Think out loud**: Explain your thought process
3. **Start simple**: Get basic solution working first
4. **Consider edge cases**: Null, empty, errors
5. **Discuss trade-offs**: Pros and cons of approach

### Banking Context
1. **Always use BigDecimal** for money
2. **Consider compliance** (PCI-DSS, SOX)
3. **Think security** (encryption, audit logs)
4. **High availability** (99.99% uptime)
5. **Idempotency** for transactions

### Common Mistakes
- ❌ Using float/double for money
- ❌ Not handling null values
- ❌ Ignoring thread safety
- ❌ Not considering performance
- ❌ Forgetting error handling

---

## 📝 Quick Formulas

### Time Complexity
- O(1): Constant
- O(log n): Logarithmic
- O(n): Linear
- O(n log n): Linearithmic
- O(n²): Quadratic

### Availability Calculation
```
Uptime % = (Total Time - Downtime) / Total Time × 100

99.9%  = 8.76 hours downtime/year
99.99% = 52.56 minutes downtime/year
```

### Throughput Calculation
```
Throughput = Number of Requests / Time Period

Example: 1000 requests / 1 second = 1000 TPS
```

---

**Print this cheat sheet for quick reference during interviews!** 📄

[← Back to Main README](README.md) | [Study Guide →](STUDY_GUIDE.md)
