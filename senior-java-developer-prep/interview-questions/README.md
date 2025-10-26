# Interview Questions - Senior Java Developer (Banking)

## Spring Boot & Microservices

### Q1: How do you set different profiles in Spring Boot?
**Answer**: Multiple ways:
- `application.properties`: `spring.profiles.active=prod`
- Environment variable: `SPRING_PROFILES_ACTIVE=prod`
- Command line: `java -jar app.jar --spring.profiles.active=prod`
- Programmatically: `SpringApplication.setAdditionalProfiles("prod")`

### Q2: Explain the difference between @RestController and @Controller
**Answer**: 
- `@RestController` = `@Controller` + `@ResponseBody`
- `@RestController` returns data (JSON/XML), not views
- Used for REST APIs in microservices

### Q3: How do you handle exceptions globally in Spring Boot?
**Answer**: Use `@ControllerAdvice` with `@ExceptionHandler`
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Q4: What's the difference between Spring Interceptor and Filter?
**Answer**:
- **Filter**: Servlet API, executes before/after servlet, access to request/response
- **Interceptor**: Spring MVC, executes before/after controller, access to handler and ModelAndView
- Use Filter for security, logging; Interceptor for business logic

### Q5: How do you implement circuit breaker in Spring Boot?
**Answer**: Use Resilience4j
```java
@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
public PaymentResponse processPayment(PaymentRequest request) {
    return externalPaymentGateway.process(request);
}
```

---

## API Security

### Q6: Explain mTLS and why banks use it
**Answer**: Mutual TLS - both client and server authenticate using certificates
- **Why banks use it**: Zero-trust security, service-to-service authentication, PCI-DSS compliance
- Prevents man-in-the-middle attacks
- Non-repudiation (cryptographic proof)

### Q7: What's the difference between POST and PUT?
**Answer**:
- **POST**: Create new resource, non-idempotent, returns 201 Created
- **PUT**: Update/replace resource, idempotent, returns 200 OK or 204 No Content
- Example: POST /accounts (create), PUT /accounts/123 (update)

### Q8: How do you secure sensitive data in application.properties?
**Answer**:
- Use HashiCorp Vault or AWS Secrets Manager
- Kubernetes Secrets
- Jasypt for encryption
- Never commit secrets to Git
- Use environment variables

### Q9: How do you implement rate limiting?
**Answer**: Multiple approaches:
- API Gateway (AWS API Gateway, Kong)
- Spring Cloud Gateway with Redis
- Bucket4j library
- Custom filter with Guava RateLimiter

### Q10: Explain JWT token structure
**Answer**: Three parts (header.payload.signature):
- **Header**: Algorithm and token type
- **Payload**: Claims (sub, exp, iat, custom claims)
- **Signature**: Verify token integrity
- Use RS256 (asymmetric) for banking, not HS256

---

## Kubernetes & OpenShift

### Q11: What is a Pod in Kubernetes?
**Answer**: Smallest deployable unit, can contain one or more containers
- Shares network namespace (same IP)
- Shares storage volumes
- Scheduled together on same node
- Lifecycle managed as single unit

### Q12: How do you scale pods in Kubernetes?
**Answer**:
- Manual: `kubectl scale deployment payment-service --replicas=5`
- Horizontal Pod Autoscaler (HPA): Auto-scale based on CPU/memory
- Vertical Pod Autoscaler (VPA): Adjust resource requests/limits

### Q13: Explain CPU and Memory limits
**Answer**:
- **Request**: Guaranteed resources, used for scheduling
- **Limit**: Maximum resources before throttling (CPU) or OOMKilled (memory)
- CPU measured in millicores (1000m = 1 core)
- Memory in Mi/Gi
- Set limits to prevent resource exhaustion

### Q14: How do you bring down a pod safely?
**Answer**:
- `kubectl delete pod <name>` - graceful termination (30s default)
- `kubectl scale deployment <name> --replicas=0` - scale down
- `kubectl drain <node>` - evict all pods from node
- Implement graceful shutdown in application

### Q15: What's the difference between liveness and readiness probes?
**Answer**:
- **Liveness**: Is container running? If fails, restart container
- **Readiness**: Is container ready for traffic? If fails, remove from service endpoints
- Example: Liveness checks app health, Readiness checks DB connection

---

## Java Fundamentals

### Q16: How do you filter and sum transactions using Streams?
**Answer**:
```java
BigDecimal total = transactions.stream()
    .filter(t -> t.getDate().equals(LocalDate.now()))
    .filter(t -> t.getStatus() == Status.COMPLETED)
    .map(Transaction::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### Q17: Why use BigDecimal instead of double for money?
**Answer**: 
- Floating-point precision errors: `0.1 + 0.2 != 0.3`
- BigDecimal provides exact decimal arithmetic
- Always set scale and rounding mode
- Critical for financial calculations

### Q18: Explain ConcurrentHashMap vs HashMap
**Answer**:
- **HashMap**: Not thread-safe, faster for single-threaded
- **ConcurrentHashMap**: Thread-safe, lock-free reads, segment locking for writes
- Use ConcurrentHashMap for caching in multi-threaded banking apps

### Q19: How do you prevent deadlock in payment processing?
**Answer**:
- Always acquire locks in same order
- Use timeouts: `lock.tryLock(5, TimeUnit.SECONDS)`
- Minimize lock duration
- Consider lock-free algorithms
- Example: Lock accounts in alphabetical order

### Q20: What's the difference between map() and flatMap()?
**Answer**:
- **map()**: One-to-one transformation, `Stream<T>` → `Stream<R>`
- **flatMap()**: One-to-many transformation, flattens nested streams
- Example: `customers.flatMap(c -> c.getAccounts().stream())`

---

## Banking Domain

### Q21: How do you ensure idempotency in payment processing?
**Answer**:
- Use idempotency keys (UUID)
- Check for duplicate transaction IDs before processing
- Database unique constraints
- Return same response for duplicate requests
- Critical to prevent double charges

### Q22: Explain ACID properties in banking transactions
**Answer**:
- **Atomicity**: All or nothing (transfer debits and credits together)
- **Consistency**: Valid state (balance never negative)
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed transactions persist

### Q23: How do you detect fraud in real-time?
**Answer**:
- Velocity checks (rapid transactions)
- Unusual spending patterns (3x average)
- Geolocation anomalies
- ML-based scoring
- Rule engine for known patterns
- Real-time streaming with Kafka

### Q24: What is PCI-DSS compliance?
**Answer**: Payment Card Industry Data Security Standard
- Protect cardholder data
- Encrypt transmission over public networks
- Restrict access to cardholder data
- Regular security testing
- Maintain security policy

### Q25: How do you handle PII (Personally Identifiable Information)?
**Answer**:
- Encrypt at rest and in transit
- Mask in logs and UI (show last 4 digits)
- Tokenization for storage
- Access control and audit logging
- GDPR/CCPA compliance
- Data retention policies

---

## CI/CD & DevOps

### Q26: Explain Jenkins declarative vs scripted pipeline
**Answer**:
- **Declarative**: Structured, easier to read, `pipeline {}` block
- **Scripted**: Groovy-based, more flexible, `node {}` block
- Declarative preferred for most use cases

### Q27: How do you implement blue-green deployment?
**Answer**:
- Run two identical environments (blue=current, green=new)
- Deploy to green, test thoroughly
- Switch traffic from blue to green
- Keep blue for quick rollback
- Zero downtime deployment

### Q28: What security scans do you run in CI/CD?
**Answer**:
- OWASP dependency check (vulnerable libraries)
- Trivy (container image scanning)
- SonarQube (code quality and security)
- Secret scanning (no hardcoded credentials)
- SAST (Static Application Security Testing)

### Q29: How do you manage secrets in Kubernetes?
**Answer**:
- Kubernetes Secrets (base64 encoded)
- HashiCorp Vault (recommended for banking)
- AWS Secrets Manager
- Sealed Secrets (encrypted in Git)
- Never commit secrets to repository

### Q30: Explain canary deployment
**Answer**:
- Deploy new version to small subset (10%)
- Monitor metrics (errors, latency)
- Gradually increase traffic if healthy
- Quick rollback if issues detected
- Reduces blast radius of bugs

---

## Scenario-Based Questions

### Q31: A payment service pod keeps crashing. How do you troubleshoot?
**Answer**:
1. Check pod status: `kubectl describe pod <name>`
2. View logs: `kubectl logs <name> --previous`
3. Check resource limits (OOMKilled?)
4. Review recent deployments
5. Check health check endpoints
6. Verify database connectivity
7. Review application logs for exceptions

### Q32: Transaction processing is slow. How do you optimize?
**Answer**:
1. Add database indexes
2. Implement caching (Redis)
3. Use connection pooling
4. Optimize queries (N+1 problem)
5. Horizontal scaling (more pods)
6. Async processing for non-critical operations
7. Database read replicas
8. Profile application (JProfiler)

### Q33: How do you handle a security breach?
**Answer**:
1. Immediately isolate affected systems
2. Alert security team and management
3. Review audit logs
4. Identify scope of breach
5. Rotate all credentials
6. Patch vulnerabilities
7. Notify affected customers (if required)
8. Post-mortem and prevention measures

### Q34: Design a payment processing microservice
**Answer**:
- **API Gateway**: Authentication, rate limiting
- **Payment Service**: Process payments, idempotency
- **Account Service**: Validate accounts, check balance
- **Fraud Detection**: Real-time scoring
- **Notification Service**: Send confirmations
- **Audit Service**: Log all transactions
- **Database**: PostgreSQL with replication
- **Message Queue**: Kafka for async processing
- **Caching**: Redis for account lookups

### Q35: How do you ensure 99.99% uptime?
**Answer**:
- Multi-region deployment
- Load balancing
- Health checks and auto-healing
- Circuit breakers
- Database replication
- Disaster recovery plan
- Monitoring and alerting
- Chaos engineering
- Regular DR drills

---

## Quick Fire Round

**Q36**: What's @Transactional?  
**A**: Manages database transactions, rollback on exception

**Q37**: What's Spring Boot Actuator?  
**A**: Production-ready features: health checks, metrics, monitoring

**Q38**: What's the difference between @Component and @Service?  
**A**: Semantic difference, @Service indicates business logic layer

**Q39**: What's a ConfigMap in Kubernetes?  
**A**: Store non-sensitive configuration data as key-value pairs

**Q40**: What's the purpose of .dockerignore?  
**A**: Exclude files from Docker build context (like .gitignore)

**Q41**: What's Spring Cloud Config?  
**A**: Centralized external configuration management

**Q42**: What's a StatefulSet?  
**A**: Kubernetes workload for stateful applications (databases)

**Q43**: What's OAuth2?  
**A**: Authorization framework for delegated access

**Q44**: What's a DaemonSet?  
**A**: Ensures pod runs on all (or some) nodes

**Q45**: What's Spring Data JPA?  
**A**: Simplifies data access, repository pattern

**Q46**: What's Helm?  
**A**: Package manager for Kubernetes

**Q47**: What's a Service Mesh?  
**A**: Infrastructure layer for service-to-service communication (Istio)

**Q48**: What's Spring Security Filter Chain?  
**A**: Series of filters for authentication and authorization

**Q49**: What's a Persistent Volume?  
**A**: Storage resource in Kubernetes, independent of pod lifecycle

**Q50**: What's the difference between RabbitMQ and Kafka?  
**A**: RabbitMQ is message broker, Kafka is distributed streaming platform

---

## Behavioral Questions

**Q51**: Describe a challenging bug you fixed in production  
**Q52**: How do you handle disagreements with team members?  
**Q53**: Tell me about a time you improved system performance  
**Q54**: How do you stay updated with new technologies?  
**Q55**: Describe your experience with Agile/Scrum

---

## Questions to Ask Interviewer

1. What's the tech stack for this team?
2. How do you handle on-call rotations?
3. What's the deployment frequency?
4. How is the team structured?
5. What are the biggest technical challenges?
6. What's the code review process?
7. How do you measure success?
8. What's the career growth path?
9. What's the team's approach to technical debt?
10. How do you balance feature development vs. maintenance?

---

## Preparation Tips

✅ Practice coding on whiteboard/screen share  
✅ Explain your thought process aloud  
✅ Ask clarifying questions  
✅ Discuss trade-offs in your solutions  
✅ Be honest about what you don't know  
✅ Show enthusiasm for learning  
✅ Prepare examples from past experience  
✅ Review recent projects and challenges  
✅ Practice system design questions  
✅ Be ready to discuss failures and learnings
