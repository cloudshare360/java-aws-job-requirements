# ⚓ Cloud, Kubernetes & OpenShift

[← Back to Main](../../README.md) | [← Previous: API Security](../api-security/README.md) | [Next: Java Fundamentals →](../java-fundamentals/README.md)

---

## 📋 Table of Contents

- [Kubernetes Fundamentals](#kubernetes-fundamentals)
  - [Pods](#pods)
  - [Deployments](#deployments)
  - [Services](#services)
- [Resource Management](#resource-management)
  - [CPU and Memory Limits](#cpu-and-memory-limits)
  - [Resource Requests](#resource-requests)
- [Health Checks](#health-checks)
  - [Liveness Probes](#liveness-probes)
  - [Readiness Probes](#readiness-probes)
- [OpenShift Specifics](#openshift-specifics)
- [Deployment Strategies](#deployment-strategies)
- [kubectl Commands](#kubectl-commands)
- [Banking Use Cases](#banking-use-cases)

---

## Overview

This section covers container orchestration, Kubernetes concepts, and OpenShift deployment strategies for financial services microservices.

**What You'll Learn:**
- ✅ Kubernetes pod management
- ✅ Deployment and scaling strategies
- ✅ Resource limits (CPU/Memory)
- ✅ Health checks (liveness/readiness)
- ✅ OpenShift-specific features
- ✅ High availability for banking apps

---

## Kubernetes Fundamentals

### Pods

#### What is a Pod?
A Pod is the smallest deployable unit in Kubernetes that can contain one or more containers sharing:
- Network namespace (same IP address)
- Storage volumes
- Configuration

#### Pod Lifecycle Phases
1. **Pending** - Pod accepted but not yet running
2. **Running** - Pod bound to node, containers created
3. **Succeeded** - All containers terminated successfully
4. **Failed** - All containers terminated, at least one failed
5. **Unknown** - Pod state cannot be determined

#### Single Container Pod Example
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: payment-service
  labels:
    app: payment
    tier: backend
spec:
  containers:
  - name: payment-app
    image: payment-service:1.0.0
    ports:
    - containerPort: 8080
    env:
    - name: SPRING_PROFILES_ACTIVE
      value: "prod"
    resources:
      requests:
        memory: "512Mi"
        cpu: "500m"
      limits:
        memory: "1Gi"
        cpu: "1000m"
```

#### Multi-Container Pod (Sidecar Pattern)
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-with-logging
spec:
  containers:
  - name: application
    image: payment-service:1.0.0
    volumeMounts:
    - name: logs
      mountPath: /var/log
  - name: log-shipper
    image: fluentd:latest
    volumeMounts:
    - name: logs
      mountPath: /var/log
  volumes:
  - name: logs
    emptyDir: {}
```

---

## Deploying to Cloud

### Deployment Resource

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: financial-services
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment
  template:
    metadata:
      labels:
        app: payment
        version: v1
    spec:
      containers:
      - name: payment
        image: payment-service:1.0.0
        ports:
        - containerPort: 8080
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
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Service Resource

```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
spec:
  selector:
    app: payment
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

### Deployment Commands

```bash
# Apply deployment
kubectl apply -f deployment.yaml

# Check deployment status
kubectl get deployments
kubectl describe deployment payment-service

# View pods
kubectl get pods -l app=payment

# Check pod logs
kubectl logs payment-service-7d8f9c5b6-xyz12

# Scale deployment
kubectl scale deployment payment-service --replicas=5

# Update image (rolling update)
kubectl set image deployment/payment-service payment=payment-service:1.1.0

# Rollback deployment
kubectl rollout undo deployment/payment-service
```

---

## Pod Management

### Bringing Down Pods

#### Delete Specific Pod
```bash
# Delete single pod (will be recreated by deployment)
kubectl delete pod payment-service-7d8f9c5b6-xyz12

# Force delete (immediate termination)
kubectl delete pod payment-service-7d8f9c5b6-xyz12 --force --grace-period=0
```

#### Scale Down Deployment
```bash
# Scale to zero replicas
kubectl scale deployment payment-service --replicas=0

# Scale back up
kubectl scale deployment payment-service --replicas=3
```

#### Delete Deployment (removes all pods)
```bash
kubectl delete deployment payment-service
```

#### Drain Node (for maintenance)
```bash
# Safely evict all pods from node
kubectl drain node-1 --ignore-daemonsets --delete-emptydir-data

# Make node schedulable again
kubectl uncordon node-1
```

### Pod Troubleshooting

```bash
# Get pod details
kubectl describe pod payment-service-7d8f9c5b6-xyz12

# View logs
kubectl logs payment-service-7d8f9c5b6-xyz12

# View previous container logs (if crashed)
kubectl logs payment-service-7d8f9c5b6-xyz12 --previous

# Execute command in pod
kubectl exec -it payment-service-7d8f9c5b6-xyz12 -- /bin/bash

# Port forward for local testing
kubectl port-forward payment-service-7d8f9c5b6-xyz12 8080:8080
```

---

## Resource Management

### CPU Limits

#### Understanding CPU Resources
- **CPU Request**: Guaranteed CPU allocation
- **CPU Limit**: Maximum CPU the container can use
- Measured in millicores (m): 1000m = 1 CPU core

#### CPU Configuration
```yaml
resources:
  requests:
    cpu: "500m"      # Guaranteed 0.5 CPU cores
  limits:
    cpu: "2000m"     # Can burst up to 2 CPU cores
```

#### CPU Throttling
- When container exceeds CPU limit, it gets throttled (not killed)
- Monitor with: `kubectl top pods`
- Check throttling: `kubectl describe pod <pod-name>` (look for CPU throttling events)

#### Best Practices
- Set requests based on average usage
- Set limits 1.5-2x higher than requests for burst capacity
- Monitor actual usage before setting limits
- Financial services: Conservative limits to ensure predictable performance

### Memory Limits

#### Understanding Memory Resources
- **Memory Request**: Guaranteed memory allocation
- **Memory Limit**: Maximum memory before OOMKilled
- Measured in bytes: Mi (Mebibytes), Gi (Gibibytes)

#### Memory Configuration
```yaml
resources:
  requests:
    memory: "512Mi"   # Guaranteed 512 MiB
  limits:
    memory: "1Gi"     # Maximum 1 GiB before OOMKilled
```

#### OOMKilled Scenarios
When a container exceeds memory limit:
1. Kubernetes kills the container (OOMKilled)
2. Pod status shows `OOMKilled`
3. Container restarts (if restart policy allows)

```bash
# Check for OOMKilled pods
kubectl get pods | grep OOMKilled

# View OOM events
kubectl describe pod <pod-name> | grep -A 5 "OOMKilled"
```

#### Memory Optimization Strategies
1. **JVM Tuning for Java Applications**
```yaml
env:
- name: JAVA_OPTS
  value: "-Xms512m -Xmx768m -XX:MaxMetaspaceSize=256m"
```

2. **Set appropriate heap size** (70-80% of memory limit)
3. **Monitor memory usage**: `kubectl top pods`
4. **Use memory profiling tools** (JProfiler, VisualVM)
5. **Implement graceful degradation** for memory pressure

#### Resource Monitoring
```bash
# View resource usage
kubectl top nodes
kubectl top pods

# Detailed metrics
kubectl describe node <node-name>
```

---

## OpenShift Specifics

### OpenShift vs Kubernetes
- OpenShift is built on Kubernetes with additional features
- Enhanced security (Security Context Constraints)
- Built-in CI/CD (OpenShift Pipelines)
- Developer-friendly UI and CLI (`oc` command)
- Integrated container registry

### OpenShift Commands

```bash
# Login to OpenShift
oc login https://api.openshift.example.com

# Create new project
oc new-project financial-services

# Deploy application
oc new-app payment-service:latest

# Expose service
oc expose svc/payment-service

# View routes
oc get routes

# Scale application
oc scale dc/payment-service --replicas=3

# View logs
oc logs -f payment-service-1-xyz12

# Start build
oc start-build payment-service
```

### OpenShift Deployment Config

```yaml
apiVersion: apps.openshift.io/v1
kind: DeploymentConfig
metadata:
  name: payment-service
spec:
  replicas: 3
  selector:
    app: payment
  template:
    metadata:
      labels:
        app: payment
    spec:
      containers:
      - name: payment
        image: payment-service:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

---

## Health Checks

### Liveness Probe
Determines if container is running. If fails, Kubernetes restarts container.

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe
Determines if container is ready to serve traffic. If fails, removes from service endpoints.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

### Spring Boot Actuator Configuration

```properties
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

---

## Best Practices for Financial Services

1. **High Availability**: Run multiple replicas across availability zones
2. **Resource Limits**: Always set both requests and limits
3. **Health Checks**: Implement comprehensive liveness and readiness probes
4. **Security**: Use Security Context Constraints (OpenShift) or Pod Security Policies
5. **Monitoring**: Integrate with Prometheus, Grafana for observability
6. **Secrets Management**: Use Kubernetes Secrets or external vaults (HashiCorp Vault)
7. **Network Policies**: Restrict pod-to-pod communication
8. **Backup & DR**: Regular backups, disaster recovery plans
9. **Rolling Updates**: Zero-downtime deployments
10. **Resource Quotas**: Prevent resource exhaustion at namespace level
