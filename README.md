# SwiftPay - Real-Time Payment Ledger

SwiftPay is a real-time peer-to-peer payment platform built using Java 21 and Spring Boot.

The system uses PostgreSQL as the financial source of truth, Kafka for asynchronous payment events, Redis for idempotency and caching, and Docker/Kubernetes for deployment.

---

## Tech Stack

| Technology        | Purpose                         |
| ----------------- | ------------------------------- |
| Java 21           | Backend development             |
| Spring Boot 3.5.6 | Microservices framework         |
| Spring Data JPA   | Database access                 |
| PostgreSQL        | Financial data and ledger       |
| Apache Kafka      | Event-driven communication      |
| Redis             | Idempotency and caching         |
| Flyway            | Database migrations             |
| Maven             | Build and dependency management |
| Docker            | Containerization                |
| Docker Compose    | Local distributed environment   |
| Kubernetes        | Container orchestration         |
| GitHub Actions    | CI/CD                           |
| k6                | Load testing                    |
| Windows PktMon    | Network packet capture          |

---

## Architecture

```text
                    Client
                      |
                      | POST /v1/payments
                      v
        +-----------------------------+
        |   Transaction Gateway       |
        |          :3001              |
        |                             |
        | REST API                    |
        | Validation                  |
        | Redis Idempotency           |
        | PostgreSQL Persistence      |
        +-------------+---------------+
                      |
                      | PaymentInitiated
                      v
               +-------------+
               |    Kafka    |
               +------+------+
                      |
                      v
        +-----------------------------+
        |      Ledger Service         |
        |          :3002              |
        |                             |
        | Atomic Settlement           |
        | Balance Validation          |
        | Debit / Credit              |
        | Transaction History         |
        +-------------+---------------+
                      |
                      | PaymentCompleted
                      v
               +-------------+
               |    Kafka    |
               +------+------+
                      |
                      v
        +-----------------------------+
        |     Analytics Worker        |
        |          :3003              |
        |                             |
        | Payment Analytics            |
        | Volume Reporting             |
        +-----------------------------+

       +----------------+    +----------------+
       |   PostgreSQL   |    |     Redis      |
       | Source of Truth|    | Idempotency    |
       | Ledger Data    |    | Cache          |
       +----------------+    +----------------+
```

---

# Services

### 1. Transaction Gateway

**Port:** `3001`

Responsibilities:

* Payment REST API
* Request validation
* Redis idempotency
* Payment persistence
* Kafka `PaymentInitiated` event publishing

Endpoint:

```text
POST /v1/payments
```

Example:

```json
{
  "sender_id": 1001,
  "receiver_id": 1002,
  "amount": 250.00,
  "currency": "INR"
}
```

---

### 2. Ledger Service

**Port:** `3002`

Responsibilities:

* Consume `PaymentInitiated`
* Validate account balances
* Atomic debit/credit
* PostgreSQL transactions
* Pessimistic account locking
* Insufficient-funds handling
* Duplicate event handling
* Transaction history

---

### 3. Analytics Worker

**Port:** `3003`

Responsibilities:

* Consume completed payment events
* Store payment analytics
* Provide payment-volume reporting

---

# Project Structure

```text
Swiftpay Java/
│
├── transaction-gateway/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/swiftpay/gateway/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── kafka/
│   │   │   │   ├── mapper/
│   │   │   │   ├── redis/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── validation/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   │
│   │   └── test/
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── ledger-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/swiftpay/ledger/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── kafka/
│   │   │   │   ├── mapper/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   │
│   │   └── test/
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── analytics-worker/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/swiftpay/analytics/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── kafka/
│   │   │   │   ├── mapper/
│   │   │   │   ├── redis/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── validation/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   │
│   │   └── test/
│   │
│   ├── Dockerfile
│   └── pom.xml
│
├── k6/
│   └── load-tests/
│       └── payment-load.js
│
├── k8s/
│   ├── infrastructure.yaml
│   ├── applications.yaml
│   └── swiftpay.yaml
│
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── AI-PROMPTING-PLAYBOOK.md
│   ├── HACKATHON-CHECKLIST.md
│   ├── PCAP-CAPTURE.md
│   └── PERFORMANCE.md
│
├── evidence/
│   ├── pcap-payment.json
│   ├── swiftpay-payment.etl
│   └── swiftpay-payment.pcapng
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── docker-compose.yml
├── pom.xml
├── .env
├── .env.example
├── Dockerfile
└── README.md
```

---

# Database

PostgreSQL is used as the primary financial database.

The ledger service owns the shared database migration history.

```text
ledger-service/
└── src/main/resources/
    └── db/migration/
        └── V1__ledger.sql
```

The services use PostgreSQL transactions for financial operations.

Money values use `BigDecimal` to avoid floating-point precision issues.

---

# Redis

Redis is used for:

* Payment idempotency
* Balance-cache support
* Temporary state

Idempotency entries are retained for 24 hours.

Redis is not treated as the financial source of truth.

---

# Kafka

Kafka is used for asynchronous payment processing.

Main event flow:

```text
Transaction Gateway
        |
        | PaymentInitiated
        v
      Kafka
        |
        v
  Ledger Service
        |
        | PaymentCompleted / PaymentFailed
        v
      Kafka
        |
        v
 Analytics Worker
```

---

# API

### Create Payment

```http
POST /v1/payments
Content-Type: application/json
```

Request:

```json
{
  "sender_id": 1001,
  "receiver_id": 1002,
  "amount": 100.00,
  "currency": "INR"
}
```

Successful payment submission:

```text
HTTP 202 Accepted
```

---

# Health Endpoints

Each service exposes:

```text
/health
/ready
```

Example:

```text
http://localhost:3001/health
http://localhost:3001/ready
```

---

# Build

## Prerequisites

* JDK 21
* Maven 3.9+
* Docker Desktop
* Kubernetes
* k6

Check Java:

```powershell
java -version
```

Check Maven:

```powershell
mvn -version
```

---

## Build Entire Project

From the project root:

```powershell
mvn clean verify
```

---

## Build Individual Services

### Transaction Gateway

```powershell
cd transaction-gateway
mvn clean verify
```

### Ledger Service

```powershell
cd ledger-service
mvn clean verify
```

### Analytics Worker

```powershell
cd analytics-worker
mvn clean verify
```

---

# Run with Docker Compose

From the project root:

```powershell
docker compose up --build
```

Run in background:

```powershell
docker compose up -d --build
```

Check containers:

```powershell
docker compose ps
```

View logs:

```powershell
docker compose logs -f
```

Stop:

```powershell
docker compose down
```

---

# Kubernetes

Apply infrastructure:

```powershell
kubectl apply -f k8s/infrastructure.yaml
```

Apply applications:

```powershell
kubectl apply -f k8s/applications.yaml
```

Or apply the combined manifest:

```powershell
kubectl apply -f k8s/swiftpay.yaml
```

Check pods:

```powershell
kubectl get pods -n swiftpay
```

Check services:

```powershell
kubectl get svc -n swiftpay
```

Check deployments:

```powershell
kubectl get deployments -n swiftpay
```

Check pod logs:

```powershell
kubectl logs -n swiftpay <pod-name>
```

---

# Testing

Run all tests:

```powershell
mvn test
```

Run gateway tests:

```powershell
cd transaction-gateway
mvn test
```

Run ledger tests:

```powershell
cd ..\ledger-service
mvn test
```

Run analytics tests:

```powershell
cd ..\analytics-worker
mvn test
```

---

# k6 Performance Testing

Load test:

```text
k6/load-tests/payment-load.js
```

Set target TPS:

```powershell
$env:TARGET_TPS="250"
```

Set total requests:

```powershell
$env:TOTAL_REQUESTS="7500"
```

Run:

```powershell
k6 run .\k6\load-tests\payment-load.js
```

Example larger run:

```powershell
$env:TARGET_TPS="250"
$env:TOTAL_REQUESTS="25000"

k6 run .\k6\load-tests\payment-load.js
```

For the assignment-scale benchmark:

```powershell
$env:TARGET_TPS="250"
$env:TOTAL_REQUESTS="1000000"

k6 run .\k6\load-tests\payment-load.js
```

At exactly 250 TPS, 1,000,000 requests require approximately:

```text
66 minutes 40 seconds
```

---

# PCAP Evidence

Packet-capture evidence is stored in:

```text
evidence/
├── pcap-payment.json
├── swiftpay-payment.etl
└── swiftpay-payment.pcapng
```

Capture instructions:

```text
docs/PCAP-CAPTURE.md
```

---

# CI/CD

GitHub Actions workflow:

```text
.github/workflows/ci.yml
```

The workflow is used for:

* Maven build
* Tests
* Docker image build

---

# Documentation

Additional documentation:

```text
docs/
├── API.md
├── ARCHITECTURE.md
├── AI-PROMPTING-PLAYBOOK.md
├── HACKATHON-CHECKLIST.md
├── PCAP-CAPTURE.md
└── PERFORMANCE.md
```

---

# Quick Start

```powershell
# Clone / open project
cd "C:\Users\King Of Lenovo\Desktop\Swiftpay Java"

# Build
mvn clean verify

# Start distributed environment
docker compose up --build

# Check services
docker compose ps

# Kubernetes deployment
kubectl apply -f k8s/infrastructure.yaml
kubectl apply -f k8s/applications.yaml

# Check Kubernetes
kubectl get pods -n swiftpay

# Run tests
mvn test

# Run k6
$env:TARGET_TPS="250"
$env:TOTAL_REQUESTS="7500"
k6 run .\k6\load-tests\payment-load.js
```

---

# Project Status

| Component                | Status       |
| ------------------------ | -------      |
| Java 21                  | DONE         |
| Spring Boot              | DONE         |
| Transaction Gateway      | DONE         |
| Ledger Service           | DONE         |
| Analytics Worker         | DONE         |
| PostgreSQL               | DONE         |
| Kafka                    | DONE         |
| Redis                    | DONE         |
| Flyway                   | DONE         |
| Docker                   | DONE         |
| Docker Compose           | DONE         |
| Kubernetes               | DONE         |
| GitHub Actions           | DONE         |
| Automated Tests          | DONE         |
| k6 Load Testing          | DONE         |
| PCAP Evidence            | DONE         |
| 1M Transaction Benchmark | Partially done|

---

## Key Design Principles

* PostgreSQL is the financial source of truth.
* Redis is used for idempotency and caching.
* Kafka provides asynchronous service communication.
* Ledger settlement uses PostgreSQL transactions.
* Account updates use deterministic pessimistic locking.
* Duplicate payment events are handled safely.
* `BigDecimal` is used for monetary values.
* Services are independently buildable and deployable.
* Docker and Kubernetes configurations are included.
