# ☕ Java Fundamentals for Banking Applications

[← Back to Main](../../README.md) | [← Previous: Kubernetes](../cloud-kubernetes-openshift/README.md) | [Next: CI/CD →](../cicd-devops/README.md)

---

## 📋 Table of Contents

- [Java Streams API](#java-streams-api)
  - [Transaction Processing](#transaction-processing)
  - [Account Management](#account-management)
  - [Fraud Detection](#fraud-detection)
- [Collections Framework](#collections-framework)
- [Optional Class](#optional-class)
- [Lambda Expressions](#lambda-expressions)
- [Concurrency](#concurrency)
- [BigDecimal for Financial Calculations](#bigdecimal-for-financial-calculations)
- [Interview Questions](#interview-questions)

---

## Overview

This section covers core Java concepts with real-world banking and financial services use cases from institutions like Wells Fargo, Chase, Bank of America, and other major banks.

**What You'll Learn:**
- ✅ Java Streams API for transaction processing
- ✅ Collections framework best practices
- ✅ BigDecimal for money calculations
- ✅ Concurrency for high-volume systems
- ✅ Optional for null safety
- ✅ Lambda expressions and functional programming

---

## Java Streams API

### Banking Use Cases for Streams

#### 1. Transaction Processing

```java
public class TransactionProcessor {
    
    // Filter high-value transactions for fraud detection
    public List<Transaction> getHighValueTransactions(
            List<Transaction> transactions, 
            BigDecimal threshold) {
        
        return transactions.stream()
            .filter(t -> t.getAmount().compareTo(threshold) > 0)
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .sorted(Comparator.comparing(Transaction::getAmount).reversed())
            .collect(Collectors.toList());
    }
    
    // Calculate daily transaction totals by account
    public Map<String, BigDecimal> getDailyTotalsByAccount(
            List<Transaction> transactions) {
        
        return transactions.stream()
            .filter(t -> t.getDate().equals(LocalDate.now()))
            .collect(Collectors.groupingBy(
                Transaction::getAccountId,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Transaction::getAmount,
                    BigDecimal::add
                )
            ));
    }
    
    // Find suspicious transaction patterns
    public List<String> findSuspiciousAccounts(
            List<Transaction> transactions) {
        
        return transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getAccountId,
                Collectors.counting()
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 50) // More than 50 transactions
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
```

#### 2. Account Management

```java
public class AccountService {
    
    // Get all active premium accounts with balance > $10,000
    public List<Account> getPremiumAccounts(List<Account> accounts) {
        return accounts.stream()
            .filter(Account::isActive)
            .filter(a -> a.getBalance().compareTo(new BigDecimal("10000")) > 0)
            .filter(a -> a.getAccountType() == AccountType.PREMIUM)
            .collect(Collectors.toList());
    }
    
    // Calculate total assets by customer
    public Map<String, BigDecimal> getTotalAssetsByCustomer(
            List<Account> accounts) {
        
        return accounts.stream()
            .collect(Collectors.groupingBy(
                Account::getCustomerId,
                Collectors.mapping(
                    Account::getBalance,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
    }
    
    // Find customers with multiple account types
    public List<String> getCustomersWithMultipleAccountTypes(
            List<Account> accounts) {
        
        return accounts.stream()
            .collect(Collectors.groupingBy(
                Account::getCustomerId,
                Collectors.mapping(
                    Account::getAccountType,
                    Collectors.toSet()
                )
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    // Get account summary statistics
    public AccountStatistics getAccountStatistics(List<Account> accounts) {
        DoubleSummaryStatistics stats = accounts.stream()
            .mapToDouble(a -> a.getBalance().doubleValue())
            .summaryStatistics();
        
        return AccountStatistics.builder()
            .totalAccounts(stats.getCount())
            .totalBalance(BigDecimal.valueOf(stats.getSum()))
            .averageBalance(BigDecimal.valueOf(stats.getAverage()))
            .minBalance(BigDecimal.valueOf(stats.getMin()))
            .maxBalance(BigDecimal.valueOf(stats.getMax()))
            .build();
    }
}
```

#### 3. Payment Processing

```java
public class PaymentService {
    
    // Process batch payments
    public List<PaymentResult> processBatchPayments(
            List<PaymentRequest> requests) {
        
        return requests.parallelStream()
            .map(this::processPayment)
            .collect(Collectors.toList());
    }
    
    // Group payments by status
    public Map<PaymentStatus, List<Payment>> groupPaymentsByStatus(
            List<Payment> payments) {
        
        return payments.stream()
            .collect(Collectors.groupingBy(Payment::getStatus));
    }
    
    // Calculate total fees by payment type
    public Map<PaymentType, BigDecimal> calculateFeesByType(
            List<Payment> payments) {
        
        return payments.stream()
            .collect(Collectors.groupingBy(
                Payment::getPaymentType,
                Collectors.mapping(
                    Payment::getFee,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
    }
    
    // Find failed payments for retry
    public List<Payment> getFailedPaymentsForRetry(
            List<Payment> payments,
            int maxRetries) {
        
        return payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.FAILED)
            .filter(p -> p.getRetryCount() < maxRetries)
            .filter(p -> p.getLastAttempt()
                .isBefore(LocalDateTime.now().minusMinutes(5)))
            .collect(Collectors.toList());
    }
}
```

#### 4. Customer Analytics

```java
public class CustomerAnalyticsService {
    
    // Find high-value customers (total balance > $100,000)
    public List<Customer> getHighValueCustomers(
            List<Customer> customers,
            List<Account> accounts) {
        
        Map<String, BigDecimal> totalBalances = accounts.stream()
            .collect(Collectors.groupingBy(
                Account::getCustomerId,
                Collectors.mapping(
                    Account::getBalance,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        return customers.stream()
            .filter(c -> totalBalances.getOrDefault(c.getId(), BigDecimal.ZERO)
                .compareTo(new BigDecimal("100000")) > 0)
            .collect(Collectors.toList());
    }
    
    // Calculate customer lifetime value
    public Map<String, BigDecimal> calculateCustomerLifetimeValue(
            List<Transaction> transactions) {
        
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.FEE)
            .collect(Collectors.groupingBy(
                Transaction::getCustomerId,
                Collectors.mapping(
                    Transaction::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
    }
    
    // Segment customers by transaction frequency
    public Map<String, Long> getCustomerTransactionFrequency(
            List<Transaction> transactions,
            LocalDate startDate,
            LocalDate endDate) {
        
        return transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate))
            .filter(t -> !t.getDate().isAfter(endDate))
            .collect(Collectors.groupingBy(
                Transaction::getCustomerId,
                Collectors.counting()
            ));
    }
}
```

#### 5. Fraud Detection

```java
public class FraudDetectionService {
    
    // Detect unusual spending patterns
    public List<String> detectUnusualSpending(
            List<Transaction> transactions,
            Map<String, BigDecimal> averageSpending) {
        
        Map<String, BigDecimal> currentSpending = transactions.stream()
            .filter(t -> t.getDate().equals(LocalDate.now()))
            .collect(Collectors.groupingBy(
                Transaction::getAccountId,
                Collectors.mapping(
                    Transaction::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
        
        return currentSpending.entrySet().stream()
            .filter(entry -> {
                BigDecimal avg = averageSpending.get(entry.getKey());
                return avg != null && 
                       entry.getValue().compareTo(avg.multiply(new BigDecimal("3"))) > 0;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    // Find rapid-fire transactions (velocity check)
    public List<Transaction> findRapidTransactions(
            List<Transaction> transactions,
            int minutesThreshold,
            int countThreshold) {
        
        return transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getAccountId))
            .values().stream()
            .flatMap(accountTransactions -> {
                List<Transaction> sorted = accountTransactions.stream()
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .collect(Collectors.toList());
                
                return findRapidSequences(sorted, minutesThreshold, countThreshold)
                    .stream();
            })
            .collect(Collectors.toList());
    }
    
    // Detect transactions from unusual locations
    public List<Transaction> detectUnusualLocations(
            List<Transaction> transactions,
            Map<String, Set<String>> customerLocations) {
        
        return transactions.stream()
            .filter(t -> {
                Set<String> knownLocations = 
                    customerLocations.get(t.getCustomerId());
                return knownLocations != null && 
                       !knownLocations.contains(t.getLocation());
            })
            .collect(Collectors.toList());
    }
}
```

---

## Optional Class for Banking

### Handling Nullable Values Safely

```java
public class AccountService {
    
    // Safe account retrieval
    public Optional<Account> findAccountById(String accountId) {
        return accountRepository.findById(accountId);
    }
    
    // Get account balance with default
    public BigDecimal getAccountBalance(String accountId) {
        return findAccountById(accountId)
            .map(Account::getBalance)
            .orElse(BigDecimal.ZERO);
    }
    
    // Get customer email with fallback
    public String getCustomerEmail(String customerId) {
        return customerRepository.findById(customerId)
            .map(Customer::getEmail)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
    
    // Chain optional operations
    public Optional<String> getAccountOwnerEmail(String accountId) {
        return findAccountById(accountId)
            .map(Account::getCustomerId)
            .flatMap(customerRepository::findById)
            .map(Customer::getEmail);
    }
    
    // Filter and process
    public List<Account> getActiveAccountsForCustomer(String customerId) {
        return customerRepository.findById(customerId)
            .map(Customer::getAccounts)
            .orElse(Collections.emptyList())
            .stream()
            .filter(Account::isActive)
            .collect(Collectors.toList());
    }
}
```

---

## Lambda Expressions

### Banking Use Cases

```java
public class TransactionValidator {
    
    // Functional interface for validation rules
    @FunctionalInterface
    interface ValidationRule {
        boolean validate(Transaction transaction);
    }
    
    // Define validation rules as lambdas
    private static final ValidationRule AMOUNT_POSITIVE = 
        t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0;
    
    private static final ValidationRule WITHIN_DAILY_LIMIT = 
        t -> t.getAmount().compareTo(new BigDecimal("10000")) <= 0;
    
    private static final ValidationRule VALID_ACCOUNT = 
        t -> t.getAccountId() != null && t.getAccountId().matches("^ACC-\\d{10}$");
    
    private static final ValidationRule NOT_DUPLICATE = 
        t -> !isDuplicate(t.getTransactionId());
    
    // Validate transaction with multiple rules
    public boolean validateTransaction(Transaction transaction) {
        List<ValidationRule> rules = Arrays.asList(
            AMOUNT_POSITIVE,
            WITHIN_DAILY_LIMIT,
            VALID_ACCOUNT,
            NOT_DUPLICATE
        );
        
        return rules.stream()
            .allMatch(rule -> rule.validate(transaction));
    }
    
    // Custom validation with lambda
    public List<Transaction> filterTransactions(
            List<Transaction> transactions,
            Predicate<Transaction> customFilter) {
        
        return transactions.stream()
            .filter(customFilter)
            .collect(Collectors.toList());
    }
    
    // Usage examples
    public void examples() {
        // Filter by amount range
        List<Transaction> mediumTransactions = filterTransactions(
            allTransactions,
            t -> t.getAmount().compareTo(new BigDecimal("100")) > 0 &&
                 t.getAmount().compareTo(new BigDecimal("1000")) < 0
        );
        
        // Filter by date range
        List<Transaction> recentTransactions = filterTransactions(
            allTransactions,
            t -> t.getDate().isAfter(LocalDate.now().minusDays(7))
        );
        
        // Complex filter
        List<Transaction> suspiciousTransactions = filterTransactions(
            allTransactions,
            t -> t.getAmount().compareTo(new BigDecimal("5000")) > 0 &&
                 t.getLocation().equals("FOREIGN") &&
                 t.getTime().getHour() < 6
        );
    }
}
```

---

## Collections Framework

### Banking Data Structures

```java
public class BankingCollections {
    
    // Thread-safe account cache
    private final ConcurrentHashMap<String, Account> accountCache = 
        new ConcurrentHashMap<>();
    
    // Transaction queue for processing
    private final BlockingQueue<Transaction> transactionQueue = 
        new LinkedBlockingQueue<>(10000);
    
    // Customer priority queue (by balance)
    private final PriorityQueue<Customer> customersByBalance = 
        new PriorityQueue<>(
            Comparator.comparing(Customer::getTotalBalance).reversed()
        );
    
    // Recent transactions (fixed size)
    private final Deque<Transaction> recentTransactions = 
        new LinkedList<>();
    
    // Account lookup by multiple keys
    private final Map<String, Account> accountsByNumber = new HashMap<>();
    private final Map<String, Set<Account>> accountsByCustomer = new HashMap<>();
    
    // Add transaction to queue
    public boolean queueTransaction(Transaction transaction) {
        try {
            return transactionQueue.offer(
                transaction, 
                5, 
                TimeUnit.SECONDS
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    // Process transactions from queue
    public void processTransactions() {
        while (true) {
            try {
                Transaction transaction = transactionQueue.take();
                processTransaction(transaction);
                
                // Keep last 100 transactions
                recentTransactions.addFirst(transaction);
                if (recentTransactions.size() > 100) {
                    recentTransactions.removeLast();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    // Cache account with expiration
    public void cacheAccount(Account account) {
        accountCache.put(account.getId(), account);
        
        // Schedule cache eviction after 5 minutes
        scheduler.schedule(
            () -> accountCache.remove(account.getId()),
            5,
            TimeUnit.MINUTES
        );
    }
    
    // Get account from cache or database
    public Account getAccount(String accountId) {
        return accountCache.computeIfAbsent(
            accountId,
            id -> accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id))
        );
    }
}
```

---

## Concurrency for Banking

### Thread-Safe Transaction Processing

```java
@Service
public class ConcurrentTransactionService {
    
    private final AtomicLong transactionCounter = new AtomicLong(0);
    private final ConcurrentHashMap<String, ReentrantLock> accountLocks = 
        new ConcurrentHashMap<>();
    
    // Process payment with account locking
    public PaymentResult processPayment(PaymentRequest request) {
        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();
        
        // Always lock in same order to prevent deadlock
        String firstLock = fromAccount.compareTo(toAccount) < 0 
            ? fromAccount : toAccount;
        String secondLock = fromAccount.compareTo(toAccount) < 0 
            ? toAccount : fromAccount;
        
        Lock lock1 = getAccountLock(firstLock);
        Lock lock2 = getAccountLock(secondLock);
        
        try {
            // Try to acquire both locks with timeout
            if (lock1.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    if (lock2.tryLock(5, TimeUnit.SECONDS)) {
                        try {
                            return executePayment(request);
                        } finally {
                            lock2.unlock();
                        }
                    }
                } finally {
                    lock1.unlock();
                }
            }
            
            throw new PaymentTimeoutException("Could not acquire locks");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("Payment interrupted", e);
        }
    }
    
    private Lock getAccountLock(String accountId) {
        return accountLocks.computeIfAbsent(
            accountId,
            id -> new ReentrantLock()
        );
    }
    
    // Parallel batch processing
    public List<PaymentResult> processBatchPayments(
            List<PaymentRequest> requests) {
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try {
            List<CompletableFuture<PaymentResult>> futures = requests.stream()
                .map(request -> CompletableFuture.supplyAsync(
                    () -> processPayment(request),
                    executor
                ))
                .collect(Collectors.toList());
            
            return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
                
        } finally {
            executor.shutdown();
        }
    }
    
    // Generate unique transaction ID
    public String generateTransactionId() {
        long counter = transactionCounter.incrementAndGet();
        return String.format("TXN-%d-%d", 
            System.currentTimeMillis(), 
            counter
        );
    }
}
```

---

## BigDecimal for Financial Calculations

### Precise Money Calculations

```java
public class FinancialCalculator {
    
    // Always use BigDecimal for money, never double/float
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    
    // Calculate interest
    public BigDecimal calculateInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            int days) {
        
        BigDecimal dailyRate = annualRate
            .divide(HUNDRED, 10, RoundingMode.HALF_UP)
            .divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        
        return principal
            .multiply(dailyRate)
            .multiply(new BigDecimal(days))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    // Calculate transaction fee
    public BigDecimal calculateFee(
            BigDecimal amount,
            BigDecimal feePercentage,
            BigDecimal minimumFee,
            BigDecimal maximumFee) {
        
        BigDecimal calculatedFee = amount
            .multiply(feePercentage)
            .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        
        // Apply min/max constraints
        if (calculatedFee.compareTo(minimumFee) < 0) {
            return minimumFee;
        }
        if (calculatedFee.compareTo(maximumFee) > 0) {
            return maximumFee;
        }
        
        return calculatedFee;
    }
    
    // Split payment among multiple accounts
    public Map<String, BigDecimal> splitPayment(
            BigDecimal totalAmount,
            List<String> accountIds) {
        
        int count = accountIds.size();
        BigDecimal perAccount = totalAmount
            .divide(new BigDecimal(count), 2, RoundingMode.DOWN);
        
        BigDecimal remainder = totalAmount
            .subtract(perAccount.multiply(new BigDecimal(count)));
        
        Map<String, BigDecimal> splits = new HashMap<>();
        for (int i = 0; i < count; i++) {
            BigDecimal amount = perAccount;
            // Add remainder to first account
            if (i == 0) {
                amount = amount.add(remainder);
            }
            splits.put(accountIds.get(i), amount);
        }
        
        return splits;
    }
    
    // Compound interest calculation
    public BigDecimal calculateCompoundInterest(
            BigDecimal principal,
            BigDecimal annualRate,
            int years,
            int compoundingPerYear) {
        
        BigDecimal rate = annualRate
            .divide(HUNDRED, 10, RoundingMode.HALF_UP)
            .divide(new BigDecimal(compoundingPerYear), 10, RoundingMode.HALF_UP);
        
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
        int totalPeriods = years * compoundingPerYear;
        
        BigDecimal amount = principal;
        for (int i = 0; i < totalPeriods; i++) {
            amount = amount.multiply(onePlusRate);
        }
        
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
```

---

## Common Interview Questions

### Streams API
1. **Q**: How would you find the top 10 customers by transaction volume?
```java
List<Customer> topCustomers = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::getCustomerId,
        Collectors.counting()
    ))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(10)
    .map(Map.Entry::getKey)
    .map(customerRepository::findById)
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());
```

2. **Q**: How do you handle parallel streams for large transaction batches?
   - **A**: Use `parallelStream()` for CPU-intensive operations, ensure thread-safety, consider ForkJoinPool size

3. **Q**: What's the difference between `map()` and `flatMap()`?
   - **A**: `map()` transforms one-to-one; `flatMap()` transforms one-to-many and flattens the result

### Collections
1. **Q**: Why use `ConcurrentHashMap` for account cache?
   - **A**: Thread-safe without locking entire map, better performance than `Collections.synchronizedMap()`

2. **Q**: When would you use `LinkedList` vs `ArrayList`?
   - **A**: `LinkedList` for frequent insertions/deletions; `ArrayList` for random access and iteration

### Concurrency
1. **Q**: How do you prevent deadlock in payment processing?
   - **A**: Always acquire locks in same order, use timeouts, consider lock-free algorithms

2. **Q**: Explain `CompletableFuture` for async payment processing
   - **A**: Non-blocking async operations, composable, exception handling, better than raw threads

### BigDecimal
1. **Q**: Why never use `double` for money?
   - **A**: Floating-point precision errors; `0.1 + 0.2 != 0.3` in binary

2. **Q**: What rounding mode for financial calculations?
   - **A**: `HALF_UP` for general use, `HALF_EVEN` (banker's rounding) for fairness

---

## Best Practices

✅ **Always use BigDecimal** for monetary values  
✅ **Use streams** for collection processing, but know when to use loops  
✅ **Prefer immutability** for thread-safety  
✅ **Use Optional** to avoid null checks  
✅ **Handle exceptions** properly in streams  
✅ **Consider performance** - parallel streams aren't always faster  
✅ **Use appropriate collections** - ConcurrentHashMap, BlockingQueue for concurrency  
✅ **Lock ordering** to prevent deadlocks  
✅ **Set scale and rounding** explicitly for BigDecimal  
✅ **Validate inputs** before processing
