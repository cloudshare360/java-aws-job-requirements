# 🔒 API Security for Banking & Financial Services

[← Back to Main](../../README.md) | [← Previous: Spring Boot](../spring-boot-microservices/README.md) | [Next: Kubernetes →](../cloud-kubernetes-openshift/README.md)

---

## 📋 Table of Contents

- [Banking Security Requirements](#banking-security-requirements)
- [mTLS Implementation](#mtls-mutual-tls-implementation)
- [JWT Authentication](#jwt-authentication)
- [OAuth2 Integration](#oauth2-integration)
- [Rate Limiting](#rate-limiting)
- [Input Validation](#input-validation)
- [Audit Logging](#audit-logging)
- [Data Encryption](#data-encryption)
- [PII Protection](#pii-protection)
- [Interview Questions](#interview-questions)

---

## Overview

This section covers API security patterns, authentication mechanisms, and compliance requirements specific to banking institutions like Wells Fargo, Chase, Bank of America, and other financial services organizations.

**What You'll Learn:**
- ✅ mTLS (Mutual TLS) implementation
- ✅ JWT token generation and validation
- ✅ OAuth2 flows for banking
- ✅ Rate limiting strategies
- ✅ PCI-DSS compliance
- ✅ Audit logging for SOX compliance

---

## Banking Security Requirements

### Regulatory Compliance
- **PCI-DSS**: Payment card data protection
- **SOX**: Financial reporting and audit trails
- **GLBA**: Customer privacy and data protection
- **KYC/AML**: Customer verification and monitoring
- **GDPR/CCPA**: Data privacy regulations

### Security Standards
- **Zero Trust Architecture**: Never trust, always verify
- **Defense in Depth**: Multiple layers of security
- **Least Privilege**: Minimal access rights
- **Encryption Everywhere**: Data at rest and in transit
- **Audit Everything**: Complete transaction trails

---

## mTLS (Mutual TLS) Implementation

### What is mTLS?
Mutual TLS provides two-way authentication where both client and server verify each other's identity using certificates.

### Why Banks Use mTLS
- **Service-to-Service Authentication**: Verify identity of calling services
- **Zero Trust**: Don't trust network, verify every connection
- **Compliance**: Required by PCI-DSS for sensitive data transmission
- **Non-Repudiation**: Cryptographic proof of communication

### mTLS Architecture in Banking

```
┌─────────────────┐                    ┌─────────────────┐
│  Payment Service│                    │ Account Service │
│                 │                    │                 │
│  Client Cert    │◄──── mTLS ────────►│  Server Cert    │
│  (Validates     │                    │  (Validates     │
│   Server)       │                    │   Client)       │
└─────────────────┘                    └─────────────────┘
```

### Spring Boot mTLS Configuration

#### application.yml
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    # Server certificate
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: payment-service
    
    # Client certificate validation (mTLS)
    client-auth: need  # or 'want' for optional
    trust-store: classpath:truststore.p12
    trust-store-password: ${TRUSTSTORE_PASSWORD}
    trust-store-type: PKCS12
    
    # TLS protocol
    protocol: TLS
    enabled-protocols: TLSv1.3,TLSv1.2
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class MtlsSecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .x509()
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .userDetailsService(userDetailsService());
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Validate certificate CN against allowed services
            if (isValidService(username)) {
                return User.withUsername(username)
                    .password("")
                    .authorities("ROLE_SERVICE")
                    .build();
            }
            throw new UsernameNotFoundException("Service not authorized");
        };
    }
    
    private boolean isValidService(String serviceName) {
        // Check against whitelist of allowed services
        List<String> allowedServices = Arrays.asList(
            "payment-service",
            "account-service",
            "fraud-detection-service"
        );
        return allowedServices.contains(serviceName);
    }
}
```

#### RestTemplate with mTLS
```java
@Configuration
public class RestTemplateConfig {
    
    @Value("${client.ssl.key-store}")
    private Resource keyStore;
    
    @Value("${client.ssl.key-store-password}")
    private String keyStorePassword;
    
    @Value("${client.ssl.trust-store}")
    private Resource trustStore;
    
    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;
    
    @Bean
    public RestTemplate mtlsRestTemplate() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
            .loadKeyMaterial(
                keyStore.getFile(),
                keyStorePassword.toCharArray(),
                keyStorePassword.toCharArray()
            )
            .loadTrustMaterial(
                trustStore.getFile(),
                trustStorePassword.toCharArray()
            )
            .build();
        
        SSLConnectionSocketFactory socketFactory = 
            new SSLConnectionSocketFactory(sslContext);
        
        HttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory(httpClient);
        
        return new RestTemplate(factory);
    }
}
```

### Certificate Management in Banking

#### Certificate Lifecycle
1. **Generation**: Create certificate signing request (CSR)
2. **Signing**: Internal CA or trusted CA signs certificate
3. **Distribution**: Securely distribute to services
4. **Rotation**: Regular rotation (every 90 days recommended)
5. **Revocation**: Immediate revocation if compromised

#### Certificate Storage
- **Development**: Local keystores (not committed to Git)
- **Production**: HashiCorp Vault, AWS Secrets Manager
- **Kubernetes**: Cert-Manager for automated certificate management

```yaml
# Kubernetes Secret for certificates
apiVersion: v1
kind: Secret
metadata:
  name: payment-service-tls
  namespace: banking
type: kubernetes.io/tls
data:
  tls.crt: <base64-encoded-cert>
  tls.key: <base64-encoded-key>
```

---

## JWT (JSON Web Token) Authentication

### JWT in Banking Applications

#### Use Cases
- **Customer Authentication**: Mobile app, web portal login
- **Session Management**: Stateless authentication
- **API Gateway**: Token-based routing and authorization
- **Microservices**: Service-to-service authentication (alternative to mTLS)

### JWT Structure for Banking

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "customer-12345",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "roles": ["CUSTOMER", "PREMIUM"],
    "accountIds": ["ACC-001", "ACC-002"],
    "iat": 1609459200,
    "exp": 1609462800,
    "iss": "https://auth.wellsfargo.com",
    "aud": "banking-api"
  },
  "signature": "..."
}
```

### JWT Implementation

#### Token Generation Service
```java
@Service
public class JwtTokenService {
    
    @Value("${jwt.private-key}")
    private RSAPrivateKey privateKey;
    
    @Value("${jwt.public-key}")
    private RSAPublicKey publicKey;
    
    @Value("${jwt.issuer}")
    private String issuer;
    
    @Value("${jwt.expiration-minutes}")
    private int expirationMinutes;
    
    public String generateToken(CustomerDetails customer) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(customer.getCustomerId())
            .claim("name", customer.getName())
            .claim("email", customer.getEmail())
            .claim("roles", customer.getRoles())
            .claim("accountIds", customer.getAccountIds())
            .build();
        
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
            .getTokenValue();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            
            // Validate expiration
            Instant expiry = jwt.getExpiresAt();
            if (expiry != null && expiry.isBefore(Instant.now())) {
                return false;
            }
            
            // Validate issuer
            if (!issuer.equals(jwt.getIssuer().toString())) {
                return false;
            }
            
            return true;
        } catch (JwtException e) {
            log.error("JWT validation failed", e);
            return false;
        }
    }
    
    public CustomerDetails extractCustomer(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        
        return CustomerDetails.builder()
            .customerId(jwt.getSubject())
            .name(jwt.getClaimAsString("name"))
            .email(jwt.getClaimAsString("email"))
            .roles(jwt.getClaimAsStringList("roles"))
            .accountIds(jwt.getClaimAsStringList("accountIds"))
            .build();
    }
}
```

#### JWT Security Filter
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && jwtTokenService.validateToken(token)) {
            CustomerDetails customer = jwtTokenService.extractCustomer(token);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    customer,
                    null,
                    customer.getAuthorities()
                );
            
            SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### JWT Best Practices for Banking

1. **Use RS256 (RSA)**: Asymmetric encryption, not HS256
2. **Short Expiration**: 15-30 minutes for access tokens
3. **Refresh Tokens**: Separate long-lived tokens for renewal
4. **Token Revocation**: Maintain blacklist for compromised tokens
5. **Secure Storage**: HttpOnly cookies, not localStorage
6. **Claims Validation**: Always validate issuer, audience, expiration
7. **Sensitive Data**: Don't include PII or account numbers in JWT

---

## OAuth2 Implementation

### OAuth2 Flows in Banking

#### Authorization Code Flow (Customer Login)
```
Customer → Login Page → Authorization Server → Redirect with Code
         → Exchange Code for Token → Access Banking APIs
```

#### Client Credentials Flow (Service-to-Service)
```
Service A → Request Token (Client ID + Secret) → Authorization Server
          → Access Token → Call Service B APIs
```

### Spring Security OAuth2 Configuration

```java
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/accounts/**").hasAuthority("SCOPE_accounts.read")
                .antMatchers("/api/payments/**").hasAuthority("SCOPE_payments.write")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = 
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter
        );
        
        return jwtAuthenticationConverter;
    }
}
```

---

## API Security Best Practices

### 1. Rate Limiting

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String clientId = extractClientId(request);
        RateLimiter limiter = limiters.computeIfAbsent(
            clientId,
            k -> RateLimiter.create(100.0) // 100 requests per second
        );
        
        if (!limiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 2. Input Validation

```java
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        // Additional business validation
        validatePaymentAmount(request.getAmount());
        validateAccountOwnership(request.getFromAccount());
        
        return ResponseEntity.ok(paymentService.process(request));
    }
}

@Data
public class PaymentRequest {
    
    @NotNull(message = "From account is required")
    @Pattern(regexp = "^ACC-\\d{10}$", message = "Invalid account format")
    private String fromAccount;
    
    @NotNull(message = "To account is required")
    @Pattern(regexp = "^ACC-\\d{10}$", message = "Invalid account format")
    private String toAccount;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @DecimalMax(value = "1000000.00", message = "Amount exceeds limit")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(USD|EUR|GBP)$", message = "Unsupported currency")
    private String currency;
}
```

### 3. Audit Logging

```java
@Aspect
@Component
public class AuditLoggingAspect {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Around("@annotation(Audited)")
    public Object logAuditEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        Authentication auth = SecurityContextHolder.getContext()
            .getAuthentication();
        String userId = auth != null ? auth.getName() : "SYSTEM";
        
        AuditLog auditLog = AuditLog.builder()
            .timestamp(Instant.now())
            .userId(userId)
            .action(methodName)
            .parameters(serializeArgs(args))
            .ipAddress(getClientIpAddress())
            .build();
        
        try {
            Object result = joinPoint.proceed();
            auditLog.setStatus("SUCCESS");
            auditLog.setResult(serializeResult(result));
            return result;
        } catch (Exception e) {
            auditLog.setStatus("FAILURE");
            auditLog.setError(e.getMessage());
            throw e;
        } finally {
            auditLogRepository.save(auditLog);
        }
    }
}

// Usage
@Service
public class PaymentService {
    
    @Audited
    public PaymentResponse processPayment(PaymentRequest request) {
        // Payment processing logic
    }
}
```

### 4. Data Encryption

```java
@Service
public class EncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    public String encrypt(String plaintext) throws Exception {
        byte[] iv = generateIV();
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), parameterSpec);
        
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and ciphertext
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    public String decrypt(String encrypted) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encrypted);
        
        // Extract IV and ciphertext
        byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), parameterSpec);
        
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
    
    private SecretKey getSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
    
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
```

### 5. PII Masking

```java
@Component
public class PiiMaskingService {
    
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }
    
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.com";
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        String maskedUsername = username.length() > 2 
            ? username.substring(0, 2) + "***"
            : "***";
        
        return maskedUsername + "@" + domain;
    }
    
    public String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return "***-**-****";
        }
        String lastFour = ssn.substring(ssn.length() - 4);
        return "***-**-" + lastFour;
    }
}
```

---

## Interview Questions

### mTLS Questions
1. **Q**: What is the difference between TLS and mTLS?
   - **A**: TLS authenticates server only; mTLS authenticates both client and server using certificates

2. **Q**: How do you handle certificate rotation in production?
   - **A**: Use cert-manager in Kubernetes, automated rotation, zero-downtime deployment, monitor expiration

3. **Q**: What happens if a client certificate expires?
   - **A**: Connection fails with SSL handshake error; implement monitoring and alerts before expiration

### JWT Questions
1. **Q**: Why use RS256 instead of HS256 for banking?
   - **A**: RS256 uses asymmetric keys; public key can be shared for verification without compromising security

2. **Q**: How do you handle JWT token revocation?
   - **A**: Maintain token blacklist in Redis, check on each request, or use short-lived tokens with refresh mechanism

3. **Q**: What should never be included in a JWT?
   - **A**: Passwords, SSN, full account numbers, or any sensitive PII

### Security Questions
1. **Q**: How do you prevent SQL injection in Spring Boot?
   - **A**: Use parameterized queries, JPA/Hibernate, never concatenate user input in queries

2. **Q**: Explain defense in depth for a payment API
   - **A**: API gateway, authentication, authorization, rate limiting, input validation, encryption, audit logging, WAF

3. **Q**: How do you secure sensitive configuration like database passwords?
   - **A**: Use HashiCorp Vault, AWS Secrets Manager, Kubernetes Secrets, never commit to Git, rotate regularly

---

## Best Practices Summary

✅ **Always use HTTPS/TLS 1.3** for all communications  
✅ **Implement mTLS** for service-to-service communication  
✅ **Use JWT with RS256** for customer authentication  
✅ **Rate limit all APIs** to prevent abuse  
✅ **Validate all inputs** with strict rules  
✅ **Encrypt sensitive data** at rest and in transit  
✅ **Mask PII** in logs and responses  
✅ **Audit all transactions** with immutable logs  
✅ **Rotate secrets regularly** (certificates, keys, passwords)  
✅ **Monitor and alert** on security events  
✅ **Follow least privilege** principle  
✅ **Test security** with penetration testing and code reviews
