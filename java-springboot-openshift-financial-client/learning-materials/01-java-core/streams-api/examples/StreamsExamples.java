import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Java Streams API Examples - Progressive Learning
 * 
 * Start from Example 1 and work your way up.
 * Each example builds upon previous concepts.
 */
public class StreamsExamples {

    // Sample data for examples
    static class Employee {
        private String name;
        private String department;
        private double salary;
        private int age;
        
        public Employee(String name, String department, double salary, int age) {
            this.name = name;
            this.department = department;
            this.salary = salary;
            this.age = age;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public double getSalary() { return salary; }
        public int getAge() { return age; }
        
        @Override
        public String toString() {
            return String.format("Employee{name='%s', dept='%s', salary=%.0f, age=%d}", 
                               name, department, salary, age);
        }
    }
    
    // Sample employee data
    private static List<Employee> employees = Arrays.asList(
        new Employee("Alice", "Engineering", 75000, 28),
        new Employee("Bob", "Engineering", 65000, 32),
        new Employee("Charlie", "Marketing", 55000, 26),
        new Employee("Diana", "Engineering", 85000, 35),
        new Employee("Eve", "HR", 50000, 29),
        new Employee("Frank", "Marketing", 60000, 31),
        new Employee("Grace", "Engineering", 90000, 40),
        new Employee("Henry", "HR", 52000, 27)
    );

    public static void main(String[] args) {
        System.out.println("🚀 Java Streams API Examples\n");
        
        // Run all examples
        example01_BasicFiltering();
        example02_Mapping();
        example03_Sorting();
        example04_Collecting();
        example05_FlatMapping();
        example06_Grouping();
        example07_Partitioning();
        example08_Reduction();
        example09_ParallelStreams();
        example10_ComplexPipeline();
    }

    /**
     * Example 1: Basic Filtering
     * Learn: filter() operation with predicates
     */
    public static void example01_BasicFiltering() {
        System.out.println("📋 Example 1: Basic Filtering");
        System.out.println("Engineers only:");
        
        employees.stream()
            .filter(emp -> emp.getDepartment().equals("Engineering"))
            .forEach(System.out::println);
            
        System.out.println("\nHigh earners (salary > 60000):");
        employees.stream()
            .filter(emp -> emp.getSalary() > 60000)
            .forEach(System.out::println);
            
        System.out.println();
    }

    /**
     * Example 2: Mapping Transformations
     * Learn: map() to transform data
     */
    public static void example02_Mapping() {
        System.out.println("📋 Example 2: Mapping");
        System.out.println("Employee names only:");
        
        List<String> names = employees.stream()
            .map(Employee::getName)
            .collect(Collectors.toList());
        names.forEach(System.out::println);
        
        System.out.println("\nSalaries in thousands:");
        employees.stream()
            .map(emp -> emp.getName() + ": $" + (emp.getSalary() / 1000) + "K")
            .forEach(System.out::println);
            
        System.out.println();
    }

    /**
     * Example 3: Sorting Operations
     * Learn: sorted() with comparators
     */
    public static void example03_Sorting() {
        System.out.println("📋 Example 3: Sorting");
        System.out.println("Sorted by salary (ascending):");
        
        employees.stream()
            .sorted(Comparator.comparing(Employee::getSalary))
            .forEach(System.out::println);
            
        System.out.println("\nSorted by name (descending):");
        employees.stream()
            .sorted(Comparator.comparing(Employee::getName).reversed())
            .limit(3)
            .forEach(System.out::println);
            
        System.out.println();
    }

    /**
     * Example 4: Collecting Results
     * Learn: Different ways to collect stream results
     */
    public static void example04_Collecting() {
        System.out.println("📋 Example 4: Collecting");
        
        // Collect to List
        List<String> departments = employees.stream()
            .map(Employee::getDepartment)
            .distinct()
            .collect(Collectors.toList());
        System.out.println("Departments: " + departments);
        
        // Collect to Set (automatic deduplication)
        Set<String> uniqueDepts = employees.stream()
            .map(Employee::getDepartment)
            .collect(Collectors.toSet());
        System.out.println("Unique departments: " + uniqueDepts);
        
        // Collect to Map
        Map<String, Double> nameToSalary = employees.stream()
            .collect(Collectors.toMap(
                Employee::getName,
                Employee::getSalary
            ));
        System.out.println("Name to Salary mapping: " + nameToSalary);
        
        System.out.println();
    }

    /**
     * Example 5: Flat Mapping
     * Learn: flatMap() for handling nested structures
     */
    public static void example05_FlatMapping() {
        System.out.println("📋 Example 5: Flat Mapping");
        
        // Example with nested lists
        List<List<String>> departmentTeams = Arrays.asList(
            Arrays.asList("Alice", "Bob", "Diana", "Grace"),  // Engineering
            Arrays.asList("Charlie", "Frank"),                 // Marketing
            Arrays.asList("Eve", "Henry")                      // HR
        );
        
        System.out.println("All team members (flattened):");
        departmentTeams.stream()
            .flatMap(Collection::stream)
            .forEach(System.out::println);
            
        // More complex example: split names into characters
        System.out.println("\nAll characters in employee names:");
        employees.stream()
            .map(Employee::getName)
            .flatMap(name -> name.chars().mapToObj(c -> (char) c))
            .distinct()
            .sorted()
            .forEach(System.out::print);
        
        System.out.println("\n");
    }

    /**
     * Example 6: Grouping Operations
     * Learn: groupingBy collector for data aggregation
     */
    public static void example06_Grouping() {
        System.out.println("📋 Example 6: Grouping");
        
        // Group by department
        Map<String, List<Employee>> byDepartment = employees.stream()
            .collect(Collectors.groupingBy(Employee::getDepartment));
        byDepartment.forEach((dept, empList) -> {
            System.out.println(dept + ": " + empList.size() + " employees");
        });
        
        // Group by salary range
        Map<String, List<Employee>> bySalaryRange = employees.stream()
            .collect(Collectors.groupingBy(emp -> {
                if (emp.getSalary() < 60000) return "Low";
                else if (emp.getSalary() < 80000) return "Medium";
                else return "High";
            }));
        System.out.println("\nGrouped by salary range:");
        bySalaryRange.forEach((range, empList) -> {
            System.out.println(range + " salary: " + empList.size() + " employees");
        });
        
        System.out.println();
    }

    /**
     * Example 7: Partitioning
     * Learn: partitioningBy for boolean-based grouping
     */
    public static void example07_Partitioning() {
        System.out.println("📋 Example 7: Partitioning");
        
        // Partition by age (young vs experienced)
        Map<Boolean, List<Employee>> partitionedByAge = employees.stream()
            .collect(Collectors.partitioningBy(emp -> emp.getAge() < 30));
            
        System.out.println("Young employees (< 30): " + 
                         partitionedByAge.get(true).size());
        System.out.println("Experienced employees (>= 30): " + 
                         partitionedByAge.get(false).size());
        
        // Partition with downstream collector
        Map<Boolean, Double> avgSalaryByAge = employees.stream()
            .collect(Collectors.partitioningBy(
                emp -> emp.getAge() < 30,
                Collectors.averagingDouble(Employee::getSalary)
            ));
        System.out.println("Average salary - Young: $" + String.format("%.0f", avgSalaryByAge.get(true)));
        System.out.println("Average salary - Experienced: $" + String.format("%.0f", avgSalaryByAge.get(false)));
        
        System.out.println();
    }

    /**
     * Example 8: Reduction Operations
     * Learn: reduce() for custom aggregations
     */
    public static void example08_Reduction() {
        System.out.println("📋 Example 8: Reduction");
        
        // Find total salary
        double totalSalary = employees.stream()
            .mapToDouble(Employee::getSalary)
            .sum();
        System.out.println("Total salary: $" + String.format("%.0f", totalSalary));
        
        // Find highest paid employee
        Optional<Employee> highestPaid = employees.stream()
            .max(Comparator.comparing(Employee::getSalary));
        highestPaid.ifPresent(emp -> 
            System.out.println("Highest paid: " + emp.getName() + " - $" + String.format("%.0f", emp.getSalary())));
        
        // Custom reduction: concatenate all names
        String allNames = employees.stream()
            .map(Employee::getName)
            .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        System.out.println("All names: " + allNames);
        
        System.out.println();
    }

    /**
     * Example 9: Parallel Streams
     * Learn: When and how to use parallel processing
     */
    public static void example09_ParallelStreams() {
        System.out.println("📋 Example 9: Parallel Streams");
        
        // Create larger dataset for meaningful parallel processing
        List<Integer> largeList = IntStream.rangeClosed(1, 1000000)
            .boxed()
            .collect(Collectors.toList());
        
        // Sequential processing
        long startTime = System.currentTimeMillis();
        long sequentialSum = largeList.stream()
            .mapToLong(Integer::longValue)
            .map(n -> n * n)  // Square each number
            .sum();
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        // Parallel processing
        startTime = System.currentTimeMillis();
        long parallelSum = largeList.parallelStream()
            .mapToLong(Integer::longValue)
            .map(n -> n * n)  // Square each number
            .sum();
        long parallelTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Sequential result: " + sequentialSum + " (Time: " + sequentialTime + "ms)");
        System.out.println("Parallel result: " + parallelSum + " (Time: " + parallelTime + "ms)");
        System.out.println("Results match: " + (sequentialSum == parallelSum));
        
        System.out.println();
    }

    /**
     * Example 10: Complex Pipeline
     * Learn: Combining multiple operations for real-world scenarios
     */
    public static void example10_ComplexPipeline() {
        System.out.println("📋 Example 10: Complex Pipeline");
        System.out.println("Department Analysis Report:");
        
        Map<String, Map<String, Object>> departmentAnalysis = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::getDepartment,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    empList -> {
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("count", empList.size());
                        stats.put("avgSalary", empList.stream()
                            .mapToDouble(Employee::getSalary)
                            .average()
                            .orElse(0.0));
                        stats.put("avgAge", empList.stream()
                            .mapToInt(Employee::getAge)
                            .average()
                            .orElse(0.0));
                        stats.put("topEarner", empList.stream()
                            .max(Comparator.comparing(Employee::getSalary))
                            .map(Employee::getName)
                            .orElse("None"));
                        return stats;
                    }
                )
            ));
        
        departmentAnalysis.forEach((dept, stats) -> {
            System.out.printf("📊 %s Department:%n", dept);
            System.out.printf("  - Employees: %d%n", stats.get("count"));
            System.out.printf("  - Average Salary: $%.0f%n", (Double) stats.get("avgSalary"));
            System.out.printf("  - Average Age: %.1f years%n", (Double) stats.get("avgAge"));
            System.out.printf("  - Top Earner: %s%n", stats.get("topEarner"));
            System.out.println();
        });
    }
}

/**
 * 🎯 Practice Exercises
 * 
 * Try these exercises after understanding the examples:
 * 
 * 1. Find the second highest paid employee in Engineering
 * 2. Get a comma-separated string of all employee names in Marketing
 * 3. Find the department with the highest average salary
 * 4. Create a Map<Integer, List<Employee>> grouped by age decade (20s, 30s, etc.)
 * 5. Find all employees whose names contain the letter 'a' (case-insensitive)
 * 6. Calculate the total salary cost for each department
 * 7. Find the oldest employee in each department
 * 8. Create a list of employees sorted by department, then by salary descending
 * 9. Check if all employees in HR earn less than $60,000
 * 10. Find the median salary across all employees
 */