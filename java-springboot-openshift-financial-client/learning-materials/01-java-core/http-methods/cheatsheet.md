# HTTP Methods - Quick Reference Cheatsheet

## 🚀 **Core HTTP Methods Comparison**

| Method | Purpose | Idempotent | Safe | Request Body | Response Body |
|--------|---------|------------|------|--------------|---------------|
| **GET** | Retrieve data | ✅ | ✅ | ❌ | ✅ |
| **POST** | Create/Process | ❌ | ❌ | ✅ | ✅ |
| **PUT** | Create/Replace | ✅ | ❌ | ✅ | Optional |
| **PATCH** | Partial Update | ❌ | ❌ | ✅ | Optional |
| **DELETE** | Remove resource | ✅ | ❌ | Optional | Optional |
| **HEAD** | Get metadata only | ✅ | ✅ | ❌ | ❌ |
| **OPTIONS** | Get allowed methods | ✅ | ✅ | ❌ | ✅ |

---

## 🎯 **PUT vs POST - The Core Differences**

### **PUT Method**
```http
PUT /users/123 HTTP/1.1
Content-Type: application/json

{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "department": "Engineering"
}
```

**Characteristics:**
- ✅ **Idempotent**: Multiple identical requests have the same effect
- 🎯 **Target**: Specific resource (usually with ID in URL)
- 🔄 **Operation**: Complete resource replacement
- 📍 **URL**: Client determines resource location
- 🆔 **Creation**: Can create if resource doesn't exist

### **POST Method**
```http
POST /users HTTP/1.1
Content-Type: application/json

{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "department": "Marketing"
}
```

**Characteristics:**
- ❌ **Non-idempotent**: Multiple requests may have different effects
- 🎯 **Target**: Collection or processing endpoint
- ➕ **Operation**: Create new resource or process data
- 📍 **URL**: Server determines resource location
- 🆔 **Creation**: Server assigns ID/location

---

## 📊 **Status Codes Quick Reference**

### **Successful Operations**
```
200 OK              - GET, PUT (update), DELETE (with body)
201 Created         - POST, PUT (creation)
202 Accepted        - Async processing started
204 No Content      - DELETE, PUT (no response body)
```

### **Client Errors**
```
400 Bad Request     - Invalid request format/data
401 Unauthorized    - Authentication required
403 Forbidden       - Access denied
404 Not Found       - Resource doesn't exist
405 Method Not Allowed - HTTP method not supported
409 Conflict        - Resource state conflict
422 Unprocessable Entity - Validation errors
```

### **Server Errors**
```
500 Internal Server Error - Server processing error
502 Bad Gateway          - Upstream server error
503 Service Unavailable  - Temporary server overload
```

---

## 🎪 **Common Patterns & Examples**

### **User Management API**
```bash
# Create new user (POST - server assigns ID)
POST /api/users
→ 201 Created, Location: /api/users/456

# Update existing user (PUT - complete replacement)
PUT /api/users/456
→ 200 OK (update) or 201 Created (if didn't exist)

# Partial update (PATCH)
PATCH /api/users/456
→ 200 OK

# Get user
GET /api/users/456
→ 200 OK

# Delete user
DELETE /api/users/456
→ 204 No Content
```

### **File Upload Patterns**
```bash
# Upload new file (POST - server determines location)
POST /api/files
Content-Type: multipart/form-data
→ 201 Created, Location: /api/files/abc123

# Replace file content (PUT - client specifies location)
PUT /api/files/report.pdf
Content-Type: application/pdf
→ 200 OK (replaced) or 201 Created (new file)
```

### **Bulk Operations**
```bash
# Bulk create (POST)
POST /api/users/bulk
[{user1}, {user2}, {user3}]
→ 201 Created

# Batch update (PATCH or PUT)
PATCH /api/users/bulk
[{id: 1, changes}, {id: 2, changes}]
→ 200 OK
```

---

## 🔧 **Decision Tree: When to Use What**

### **Creating Resources**
```
Need to create a resource?
├── Know the exact ID/location?
│   ├── YES → Use PUT /resource/id
│   └── NO → Use POST /resources
└── Processing data (no resource creation)?
    └── Use POST /action-endpoint
```

### **Updating Resources**
```
Need to update a resource?
├── Complete replacement?
│   └── Use PUT /resource/id
├── Partial update?
│   └── Use PATCH /resource/id
└── Complex update logic?
    └── Use POST /resource/id/action
```

---

## ⚡ **Idempotency Examples**

### **Idempotent Operations (Safe to Retry)**
```bash
# PUT - Same result every time
PUT /api/users/123 {"name": "John", "age": 30}
PUT /api/users/123 {"name": "John", "age": 30}  # Same effect
PUT /api/users/123 {"name": "John", "age": 30}  # Same effect

# DELETE - Same result (even if already deleted)
DELETE /api/users/123  # User deleted
DELETE /api/users/123  # 404, but idempotent (desired state achieved)

# GET - No side effects
GET /api/users/123  # Always returns same user (if exists)
```

### **Non-Idempotent Operations (Not Safe to Retry)**
```bash
# POST - Different result each time
POST /api/users {"name": "John"}  # Creates user with ID 1
POST /api/users {"name": "John"}  # Creates user with ID 2
POST /api/users {"name": "John"}  # Creates user with ID 3

# POST for actions
POST /api/accounts/123/transfer {"amount": 100, "to": 456}
# Multiple calls = multiple transfers!
```

---

## 🛠️ **Implementation Examples**

### **Spring Boot Controller Examples**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // POST - Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.save(user);  // Server assigns ID
        return ResponseEntity.status(201)
            .location(URI.create("/api/users/" + saved.getId()))
            .body(saved);
    }
    
    // PUT - Create or replace user
    @PutMapping("/{id}")
    public ResponseEntity<User> createOrUpdateUser(
            @PathVariable Long id, 
            @RequestBody User user) {
        user.setId(id);  // Ensure ID matches path
        boolean existed = userService.exists(id);
        User saved = userService.save(user);
        
        if (existed) {
            return ResponseEntity.ok(saved);  // 200 - Updated
        } else {
            return ResponseEntity.status(201).body(saved);  // 201 - Created
        }
    }
    
    // PATCH - Partial update
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> updates) {
        User updated = userService.partialUpdate(id, updates);
        return ResponseEntity.ok(updated);
    }
}
```

### **Error Handling Patterns**
```java
// Validation errors
@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody User user, 
                                   BindingResult result) {
    if (result.hasErrors()) {
        return ResponseEntity.status(422)  // Unprocessable Entity
            .body(new ValidationErrorResponse(result.getAllErrors()));
    }
    // ... create user
}

// Conflict handling
@PutMapping("/{id}")
public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                   @RequestBody User user) {
    try {
        User updated = userService.update(id, user);
        return ResponseEntity.ok(updated);
    } catch (OptimisticLockException e) {
        return ResponseEntity.status(409)  // Conflict
            .body(new ErrorResponse("Resource was modified by another process"));
    }
}
```

---

## 🎯 **Interview Quick Prep**

### **Must-Know Concepts**
1. **Idempotency**: PUT/DELETE are idempotent, POST is not
2. **Safety**: GET/HEAD/OPTIONS don't modify server state
3. **PUT vs POST**: PUT for known location, POST for server-assigned location
4. **Status Codes**: 200 (update), 201 (create), 204 (no content), 404 (not found)

### **Common Interview Questions**
```
Q: "When do you use PUT vs POST?"
A: PUT when client knows the resource location/ID, POST when server assigns it.

Q: "What does idempotent mean?"
A: Multiple identical requests have the same effect as a single request.

Q: "What status code for creating a resource?"
A: 201 Created (with Location header pointing to new resource).

Q: "How do you handle partial updates?"
A: Use PATCH for partial updates, PUT for complete replacement.
```

### **Design Challenge Pattern**
```
"Design an API for managing blog posts"

GET    /posts           # List all posts
POST   /posts           # Create new post (server assigns ID)
GET    /posts/{id}      # Get specific post
PUT    /posts/{id}      # Replace entire post
PATCH  /posts/{id}      # Update specific fields
DELETE /posts/{id}      # Delete post

POST   /posts/{id}/publish    # Action: publish post
POST   /posts/{id}/comments   # Create comment on post
```

---

## 🚨 **Common Mistakes to Avoid**

### **❌ Don't Do This**
```bash
# Using POST for everything
POST /getUserById
POST /updateUser
POST /deleteUser

# Inconsistent status codes
PUT /users/123 → 200 OK (should be 200 for update, 201 for create)

# Non-RESTful URLs
GET /api/getUserByEmail?email=john@example.com
POST /api/createUser
PUT /api/updateUserById

# Breaking idempotency
PUT /users/123/incrementLoginCount  # Should be POST!
```

### **✅ Do This Instead**
```bash
# Use appropriate methods
GET /users/123
PUT /users/123
DELETE /users/123

# Consistent status codes
PUT /users/123 → 201 Created (new) or 200 OK (updated)

# RESTful URLs
GET /api/users?email=john@example.com
POST /api/users
PUT /api/users/123

# Maintain idempotency
POST /users/123/login  # For actions with side effects
```

---

## 🔗 **Quick Reference Links**
- [HTTP Method Registry](https://www.iana.org/assignments/http-methods/http-methods.xhtml)
- [REST API Design Guide](https://restfulapi.net/)
- [HTTP Status Codes](https://httpstatuses.com/)

**💡 Tip**: When in doubt, follow the principle of least surprise - use methods as they're commonly expected! 🎯