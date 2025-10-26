Certainly! Below is a **consolidated, well-organized Markdown document** that preserves your original content **"as-is"** under a master section titled **"Raw Job Requirements (Provided by User)"**, followed by a **structured and categorized version** for clarity and study. This format ensures you retain the original wording while also benefiting from a logically grouped reference.

```markdown
# Job Interview Preparation – Consolidated Reference

## 📄 Raw Job Requirements (Provided by User)

> GitHub actions  
> Jenkins pipeline  
> spring boot  
> spring security Spring interceptors, filters, some common annotations like entity, configuration jpa How do set profiles in spring boot How to set environment properties in Spring boot CPU limits memory limits We use Open Shift here.. so few Q on Kubernetes.. how to deploy on cloud, what are pods and how to bring down pods, mTLS connections How to secure API connections.. PUT vs POST Basic Java stream Qs Brush up on these

---

## 🧩 Organized & Categorized Breakdown

### 1. **CI/CD & Build Automation**
- GitHub Actions
- Jenkins Pipeline

### 2. **Spring Boot**
- Core concepts and configuration
- Common annotations:
  - `@Entity`
  - `@Configuration`
  - JPA-related annotations
- Setting **Spring Profiles**:
  - `application-{profile}.properties` / `.yml`
  - `spring.profiles.active` via env var, CLI, or config file
- Managing **environment properties**:
  - Externalized configuration (files, env vars, command line)

### 3. **Spring Security**
- Interceptors and Filters
- Securing applications and APIs
- Authentication & Authorization mechanisms

### 4. **Cloud & Kubernetes (OpenShift Focus)**
- Deploying Spring Boot apps to cloud (especially OpenShift)
- Kubernetes fundamentals:
  - What is a **Pod**?
  - How to **bring down a Pod**:
    ```sh
    oc delete pod <pod-name>  # OpenShift CLI
    kubectl delete pod <pod-name>  # Kubernetes (if needed)
    ```
- Resource management:
  - Setting **CPU limits**
  - Setting **memory limits** (via `resources.limits` in DeploymentConfig or Pod spec)

### 5. **API Design & Security**
- Securing API connections
- **mTLS (mutual TLS)** for service-to-service authentication
- REST semantics:
  - **PUT vs POST**: idempotency, resource creation vs update

### 6. **Core Java**
- Basic **Java Stream** questions
- Brush up on functional programming features (e.g., `map`, `filter`, `collect`)

---

> 💡 **Usage Tip**:  
> - Use the **Raw** section to ensure nothing from the original ask is lost.  
> - Use the **Organized** section for study planning, flashcards, or mock interview prep.  
> - Commit this file to your Git repo (e.g., `docs/job-prep-topics.md`) for versioned tracking.

```

This Markdown file is ready to be saved directly into your repository. It maintains fidelity to your input while offering immediate practical value through structure. Let me know if you'd like a version without the raw section or exported as a downloadable `.md` file!