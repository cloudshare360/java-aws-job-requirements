# CI/CD & DevOps for Banking Applications

## Overview
CI/CD practices for Spring Boot microservices in banking environments (Wells Fargo, Chase, Bank of America).

---

## Jenkins Pipeline

### Declarative Pipeline for Banking App

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'registry.bank.com'
        APP_NAME = 'payment-service'
        SONAR_URL = 'https://sonar.bank.com'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/bank/payment-service.git',
                    credentialsId: 'github-credentials'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
                junit 'target/surefire-reports/*.xml'
            }
        }
        
        stage('Code Quality') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                // OWASP Dependency Check
                sh 'mvn dependency-check:check'
                
                // Trivy container scan
                sh 'trivy image ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} .
                    docker tag ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} \
                               ${DOCKER_REGISTRY}/${APP_NAME}:latest
                """
            }
        }
        
        stage('Push to Registry') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry',
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh """
                        echo \$PASS | docker login -u \$USER --password-stdin ${DOCKER_REGISTRY}
                        docker push ${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/${APP_NAME}:latest
                    """
                }
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                sh """
                    kubectl set image deployment/${APP_NAME} \
                        ${APP_NAME}=${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} \
                        -n dev
                    kubectl rollout status deployment/${APP_NAME} -n dev
                """
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -Pintegration-tests'
            }
        }
        
        stage('Deploy to Prod') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                
                sh """
                    kubectl set image deployment/${APP_NAME} \
                        ${APP_NAME}=${DOCKER_REGISTRY}/${APP_NAME}:${BUILD_NUMBER} \
                        -n prod
                    kubectl rollout status deployment/${APP_NAME} -n prod
                """
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            slackSend color: 'good',
                      message: "Build ${BUILD_NUMBER} succeeded"
        }
        failure {
            slackSend color: 'danger',
                      message: "Build ${BUILD_NUMBER} failed"
        }
    }
}
```

---

## GitHub Actions

### Banking Application Workflow

```yaml
name: Banking App CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Run unit tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn verify -Pintegration-tests
    
    - name: Code coverage
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
  
  security-scan:
    runs-on: ubuntu-latest
    needs: build-and-test
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: OWASP Dependency Check
      run: mvn dependency-check:check
  
  build-and-push-image:
    runs-on: ubuntu-latest
    needs: [build-and-test, security-scan]
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: |
          ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
  
  deploy-to-kubernetes:
    runs-on: ubuntu-latest
    needs: build-and-push-image
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG }}
    
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/payment-service \
          payment-service=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} \
          -n production
        kubectl rollout status deployment/payment-service -n production
```

---

## Banking Compliance in CI/CD

### Security Gates

1. **Code Quality Gate**
   - SonarQube quality gate must pass
   - Code coverage > 80%
   - No critical vulnerabilities

2. **Security Scanning**
   - OWASP dependency check
   - Container image scanning (Trivy)
   - SAST (Static Application Security Testing)
   - Secret scanning

3. **Compliance Checks**
   - PCI-DSS compliance validation
   - License compliance
   - Audit logging enabled

### Secrets Management

```groovy
// Jenkins - HashiCorp Vault
withVault([
    vaultSecrets: [
        [path: 'secret/banking/db', 
         secretValues: [
            [envVar: 'DB_PASSWORD', vaultKey: 'password']
         ]]
    ]
]) {
    sh 'mvn deploy'
}
```

---

## Deployment Strategies

### Blue-Green Deployment

```yaml
# Blue deployment (current)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-blue
spec:
  replicas: 3
  template:
    metadata:
      labels:
        app: payment-service
        version: blue

---
# Green deployment (new)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-green
spec:
  replicas: 3
  template:
    metadata:
      labels:
        app: payment-service
        version: green

---
# Service switches between blue/green
apiVersion: v1
kind: Service
metadata:
  name: payment-service
spec:
  selector:
    app: payment-service
    version: blue  # Switch to 'green' after validation
```

### Canary Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-stable
spec:
  replicas: 9  # 90% traffic

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service-canary
spec:
  replicas: 1  # 10% traffic
```

---

## Best Practices

✅ **Automated Testing**: Unit, integration, E2E tests  
✅ **Security Scanning**: Every build  
✅ **Secrets Management**: Vault, not in code  
✅ **Immutable Artifacts**: Tag with commit SHA  
✅ **Rollback Strategy**: Quick rollback capability  
✅ **Audit Trail**: Log all deployments  
✅ **Approval Gates**: Manual approval for prod  
✅ **Monitoring**: Integrate with observability tools  
✅ **Compliance**: PCI-DSS, SOX validation  
✅ **Zero Downtime**: Rolling updates, health checks
