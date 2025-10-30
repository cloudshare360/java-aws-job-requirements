// Spring Boot Core Examples - Progressive Learning
// Build these examples in order to master Spring Boot fundamentals

/**
 * Example 1: Basic Spring Boot Application
 * Learn: @SpringBootApplication, auto-configuration, embedded server
 */

// Main Application Class
@SpringBootApplication
public class LearningApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class, args);
    }
    
    // Custom configuration bean
    @Bean
    public CommandLineRunner demo() {
        return args -> {
            System.out.println("Spring Boot Application Started!");
        };
    }
}

/**
 * Example 2: REST Controller Basics
 * Learn: @RestController, @RequestMapping, HTTP methods
 */

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private List<User> users = new ArrayList<>();
    private Long nextId = 1L;
    
    // GET all users
    @GetMapping
    public List<User> getAllUsers() {
        return users;
    }
    
    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = users.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // POST - Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setId(nextId++);
        users.add(user);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/api/users/" + user.getId()))
            .body(user);
    }
    
    // PUT - Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                          @RequestBody User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                updatedUser.setId(id);
                users.set(i, updatedUser);
                return ResponseEntity.ok(updatedUser);
            }
        }
        
        // Create new user if not found
        updatedUser.setId(id);
        users.add(updatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }
    
    // DELETE user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

// User Entity (POJO)
public class User {
    private Long id;
    private String name;
    private String email;
    private String department;
    
    // Constructors
    public User() {}
    
    public User(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', department='%s'}", 
                           id, name, email, department);
    }
}

/**
 * Example 3: Configuration Properties
 * Learn: @ConfigurationProperties, type-safe configuration
 */

@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {
    
    private String name = "Learning App";
    private String version = "1.0.0";
    private boolean debugMode = false;
    private Database database = new Database();
    private Security security = new Security();
    
    // Nested configuration
    public static class Database {
        private String url = "jdbc:h2:mem:testdb";
        private String username = "sa";
        private String password = "";
        private int maxConnections = 10;
        
        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    }
    
    public static class Security {
        private boolean enabled = true;
        private String[] allowedOrigins = {"http://localhost:3000"};
        private int sessionTimeout = 30;
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String[] getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String[] allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        
        public int getSessionTimeout() { return sessionTimeout; }
        public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
    }
    
    // Main getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    
    public Database getDatabase() { return database; }
    public void setDatabase(Database database) { this.database = database; }
    
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
}

/**
 * Example 4: Profile-based Configuration
 * Learn: @Profile, environment-specific beans
 */

@Configuration
public class ProfileConfig {
    
    @Bean
    @Profile("development")
    public DataSource developmentDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:devdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }
    
    @Bean
    @Profile("production")
    public DataSource productionDataSource(AppConfig appConfig) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(appConfig.getDatabase().getUrl());
        dataSource.setUsername(appConfig.getDatabase().getUsername());
        dataSource.setPassword(appConfig.getDatabase().getPassword());
        dataSource.setMaximumPoolSize(appConfig.getDatabase().getMaxConnections());
        return dataSource;
    }
    
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setMaximumPoolSize(2);
        return dataSource;
    }
}

/**
 * Example 5: JPA Entity and Repository
 * Learn: @Entity, @Repository, Spring Data JPA
 */

@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 50)
    private String department;
    
    @Column(nullable = false)
    private Double salary;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JPA lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (hireDate == null) {
            hireDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Employee() {}
    
    public Employee(String name, String email, String department, Double salary) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.hireDate = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

// Repository Interface
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Query methods (Spring Data JPA auto-implementation)
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByDepartmentAndSalaryGreaterThan(String department, Double salary);
    
    List<Employee> findByNameContainingIgnoreCase(String namePattern);
    
    @Query("SELECT e FROM Employee e WHERE e.hireDate >= :startDate")
    List<Employee> findEmployeesHiredAfter(@Param("startDate") LocalDate startDate);
    
    @Query("SELECT e.department, AVG(e.salary) FROM Employee e GROUP BY e.department")
    List<Object[]> findAverageSalaryByDepartment();
    
    // Custom query with pagination
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
    Page<Employee> findBySalaryRange(@Param("minSalary") Double minSalary, 
                                   @Param("maxSalary") Double maxSalary, 
                                   Pageable pageable);
    
    // Native SQL query
    @Query(value = "SELECT * FROM employees WHERE YEAR(hire_date) = :year", nativeQuery = true)
    List<Employee> findEmployeesHiredInYear(@Param("year") int year);
    
    // Update query
    @Modifying
    @Query("UPDATE Employee e SET e.salary = e.salary * 1.1 WHERE e.department = :department")
    int giveDepartmentRaise(@Param("department") String department);
}

/**
 * Example 6: Service Layer with Business Logic
 * Learn: @Service, @Transactional, business logic patterns
 */

@Service
@Transactional
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        logger.debug("Fetching all employees");
        return employeeRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        logger.debug("Fetching employee with id: {}", id);
        return employeeRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        logger.debug("Fetching employees in department: {}", department);
        return employeeRepository.findByDepartment(department);
    }
    
    public Employee createEmployee(Employee employee) {
        logger.info("Creating new employee: {}", employee.getName());
        
        // Business logic validation
        if (employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Employee with email already exists");
        }
        
        return employeeRepository.save(employee);
    }
    
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        logger.info("Updating employee with id: {}", id);
        
        return employeeRepository.findById(id)
            .map(employee -> {
                employee.setName(updatedEmployee.getName());
                employee.setEmail(updatedEmployee.getEmail());
                employee.setDepartment(updatedEmployee.getDepartment());
                employee.setSalary(updatedEmployee.getSalary());
                return employeeRepository.save(employee);
            })
            .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
    }
    
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with id: {}", id);
        
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("Employee not found with id: " + id);
        }
        
        employeeRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<Employee> getEmployeesBySalaryRange(Double minSalary, Double maxSalary, 
                                                   int page, int size, String sortBy) {
        logger.debug("Fetching employees with salary between {} and {}", minSalary, maxSalary);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return employeeRepository.findBySalaryRange(minSalary, maxSalary, pageable);
    }
    
    public int giveDepartmentRaise(String department) {
        logger.info("Giving 10% raise to all employees in department: {}", department);
        return employeeRepository.giveDepartmentRaise(department);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Double> getAverageSalaryByDepartment() {
        logger.debug("Calculating average salary by department");
        
        List<Object[]> results = employeeRepository.findAverageSalaryByDepartment();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Double) row[1]
            ));
    }
}

/**
 * Example 7: Exception Handling
 * Learn: @ControllerAdvice, @ExceptionHandler, error responses
 */

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        logger.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "ENTITY_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            "VALIDATION_FAILED",
            "Request validation failed",
            errors,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// Error Response DTOs
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String code, String message, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

public class ValidationErrorResponse extends ErrorResponse {
    private List<String> errors;
    
    public ValidationErrorResponse(String code, String message, List<String> errors, LocalDateTime timestamp) {
        super(code, message, timestamp);
        this.errors = errors;
    }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}

/**
 * 🎯 Practice Exercises
 * 
 * Try these exercises after understanding the examples:
 * 
 * 1. Add validation annotations to Employee entity (@NotNull, @Email, etc.)
 * 2. Create a Department entity with @OneToMany relationship to Employee
 * 3. Implement caching with @Cacheable on service methods
 * 4. Add @Async methods for long-running operations
 * 5. Create custom @Conditional beans based on properties
 * 6. Implement audit logging with @EntityListeners
 * 7. Add integration tests with @SpringBootTest
 * 8. Create health check indicators with @Component
 * 9. Implement custom property validation with @Validated
 * 10. Add API documentation with OpenAPI/Swagger annotations
 */