# Java Streams API - Quick Reference Cheatsheet

## 🚀 **Core Concepts**

### **Stream vs Collection**
```java
Collection<T>          Stream<T>
- Stores data          - Processes data
- Eagerly evaluated    - Lazily evaluated
- Can be iterated ∞    - Can be consumed once
- External iteration   - Internal iteration
```

### **Stream Pipeline**
```
Source → Intermediate Operations → Terminal Operation
```

---

## 📝 **Quick Syntax Reference**

### **Creating Streams**
```java
// From Collections
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> stream = list.stream();

// From Arrays
String[] array = {"a", "b", "c"};
Stream<String> stream = Arrays.stream(array);

// Empty Stream
Stream<String> empty = Stream.empty();

// Infinite Streams
Stream<Integer> infinite = Stream.iterate(0, n -> n + 2);
Stream<Double> random = Stream.generate(Math::random);

// Range Streams
IntStream range = IntStream.range(1, 10);     // 1 to 9
IntStream rangeClosed = IntStream.rangeClosed(1, 10); // 1 to 10
```

### **Intermediate Operations** (Lazy)
```java
// Filtering
.filter(x -> x > 5)
.filter(Objects::nonNull)

// Mapping
.map(String::toUpperCase)
.map(x -> x * 2)
.mapToInt(String::length)
.mapToDouble(Integer::doubleValue)

// Flattening
.flatMap(Collection::stream)
.flatMap(str -> Arrays.stream(str.split("")))

// Sorting
.sorted()
.sorted(Comparator.comparing(Person::getAge))
.sorted(Comparator.comparing(Person::getName).reversed())

// Distinct & Limiting
.distinct()
.limit(5)
.skip(3)
.peek(System.out::println)  // For debugging
```

### **Terminal Operations** (Eager)
```java
// Collecting
.collect(Collectors.toList())
.collect(Collectors.toSet())
.collect(Collectors.joining(", "))
.collect(Collectors.groupingBy(Person::getDepartment))

// Reducing
.reduce((a, b) -> a + b)
.reduce(0, Integer::sum)
.min(Comparator.comparing(Person::getAge))
.max(Comparator.comparing(Person::getSalary))

// Matching
.anyMatch(x -> x > 10)
.allMatch(x -> x > 0)
.noneMatch(x -> x < 0)

// Finding
.findFirst()
.findAny()

// Counting & Statistics
.count()
.forEach(System.out::println)
.forEachOrdered(System.out::println)  // For parallel streams
```

---

## 🎯 **Common Patterns**

### **Filtering & Transforming**
```java
// Get names of all adults
List<String> adultNames = people.stream()
    .filter(person -> person.getAge() >= 18)
    .map(Person::getName)
    .collect(Collectors.toList());
```

### **Grouping**
```java
// Group by department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Group and count
Map<String, Long> deptCounts = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()
    ));

// Group and calculate average
Map<String, Double> avgSalaries = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ));
```

### **Partitioning**
```java
// Split into two groups based on condition
Map<Boolean, List<Employee>> partitioned = employees.stream()
    .collect(Collectors.partitioningBy(emp -> emp.getSalary() > 50000));

List<Employee> highEarners = partitioned.get(true);
List<Employee> lowEarners = partitioned.get(false);
```

### **Complex Reductions**
```java
// Find employee with highest salary
Optional<Employee> topEarner = employees.stream()
    .max(Comparator.comparing(Employee::getSalary));

// Calculate total salary
double totalSalary = employees.stream()
    .mapToDouble(Employee::getSalary)
    .sum();

// Custom reduction
String concatenated = strings.stream()
    .reduce("", (a, b) -> a + b);
```

---

## ⚡ **Performance Tips**

### **Use Primitive Streams**
```java
// ❌ Avoid boxing/unboxing
Stream<Integer> boxed = list.stream().map(x -> x * 2);

// ✅ Use primitive streams
IntStream primitive = list.stream().mapToInt(x -> x * 2);
```

### **Parallel Streams Guidelines**
```java
// ✅ Good for parallel:
// - Large datasets (> 10,000 elements)
// - CPU-intensive operations
// - Stateless operations

// ❌ Avoid parallel for:
// - Small datasets
// - I/O operations
// - Stateful operations (limit, skip, sorted)

// Usage
list.parallelStream()
    .filter(expensiveOperation)
    .collect(Collectors.toList());
```

### **Short-circuiting Operations**
```java
// Use these to stop processing early
.anyMatch(condition)
.allMatch(condition)
.noneMatch(condition)
.findFirst()
.findAny()
.limit(n)
```

---

## 🔧 **Common Collectors**

### **Basic Collectors**
```java
.collect(Collectors.toList())
.collect(Collectors.toSet())
.collect(Collectors.toCollection(ArrayList::new))
.collect(Collectors.joining())
.collect(Collectors.joining(", "))
.collect(Collectors.joining(", ", "[", "]"))
```

### **Statistical Collectors**
```java
.collect(Collectors.counting())
.collect(Collectors.summingInt(Person::getAge))
.collect(Collectors.averagingDouble(Person::getSalary))
.collect(Collectors.summarizingInt(Person::getAge))
```

### **Grouping Collectors**
```java
// Simple grouping
.collect(Collectors.groupingBy(Person::getDepartment))

// Grouping with downstream collector
.collect(Collectors.groupingBy(
    Person::getDepartment,
    Collectors.counting()
))

// Multi-level grouping
.collect(Collectors.groupingBy(
    Person::getDepartment,
    Collectors.groupingBy(Person::getLevel)
))
```

### **Advanced Collectors**
```java
// To Map
.collect(Collectors.toMap(
    Person::getId,
    Person::getName
))

// Partitioning
.collect(Collectors.partitioningBy(person -> person.getAge() > 30))

// Custom collector
.collect(Collector.of(
    StringBuilder::new,
    (sb, s) -> sb.append(s),
    (sb1, sb2) -> sb1.append(sb2),
    StringBuilder::toString
))
```

---

## 🚨 **Common Pitfalls**

### **❌ Don't Do This**
```java
// Modifying source during stream operation
list.stream().forEach(item -> list.remove(item)); // ConcurrentModificationException

// Using streams for simple iterations
list.stream().forEach(System.out::println); // Just use list.forEach()

// Reusing streams
Stream<String> stream = list.stream();
stream.forEach(System.out::println);
stream.forEach(System.out::println); // IllegalStateException
```

### **✅ Do This Instead**
```java
// Use removeIf for modifications
list.removeIf(condition);

// Use simple forEach for basic iteration
list.forEach(System.out::println);

// Create new stream for each operation
list.stream().forEach(System.out::println);
list.stream().map(String::toUpperCase).forEach(System.out::println);
```

---

## 🎯 **Interview Quick Prep**

### **Must-Know Questions**
1. **Difference between map() and flatMap()**
   - `map()`: 1-to-1 transformation
   - `flatMap()`: 1-to-many transformation, flattens results

2. **Lazy vs Eager evaluation**
   - Intermediate operations are lazy (not executed until terminal operation)
   - Terminal operations are eager (trigger execution)

3. **When to use parallel streams**
   - Large datasets, CPU-intensive operations, stateless operations
   - Avoid for I/O operations, small datasets, stateful operations

4. **Common collectors**
   - `toList()`, `groupingBy()`, `partitioningBy()`, `joining()`

### **Code Challenge Pattern**
```java
// Typical interview question:
// "Find the top 3 departments by average salary"
Map<String, Double> top3Departments = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
    .limit(3)
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (e1, e2) -> e1,
        LinkedHashMap::new
    ));
```

---

## 🔗 **Quick Reference Links**
- [Stream JavaDoc](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Stream.html)
- [Collectors JavaDoc](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Collectors.html)

**💡 Tip**: Practice these patterns until they become second nature! 🚀