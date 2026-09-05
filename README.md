# SwiftPay - Real-Time Payment Ledger

SwiftPay is a Java 21 and Spring Boot peer-to-peer payment ledger. PostgreSQL is the financial source of truth; Kafka handles asynchronous payment events; Redis provides idempotency and balance-cache support.

## Current Status

**Implemented:**

- Maven parent project with three independently buildable Spring Boot services
- Java 21 and Spring Boot 3.5.6 baseline
- Gateway `POST /v1/payments` with validation, Redis idempotency, PostgreSQL persistence, and Kafka initiation events
- Ledger settlement with deterministic pessimistic locking, atomic debit/credit, insufficient-funds failure, duplicate-event handling, and after-commit side effects
- Transaction history pagination and analytics volume endpoint
- Flyway migrations with deterministic seed accounts
- Docker Compose, complete Kubernetes manifests, GitHub Actions workflow, configurable k6 script, and PCAP capture guidance
- Focused service unit tests and gateway MVC integration tests

**Not yet implemented:**

- Production secret management and authentication
- Full Testcontainers integration suite
- Operational execution of the distributed stack in this environment

Ledger service is the Flyway migration owner for the shared database. Gateway and analytics disable their duplicate migration locations in Compose to avoid competing `V1` histories; their JPA mappings use the schema created by ledger-service. Kubernetes resources are split across `k8s/swiftpay.yaml`, `k8s/infrastructure.yaml`, and `k8s/applications.yaml`.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| transaction-gateway | 3001 | REST payment entry point and idempotency |
| ledger-service | 3002 | Atomic settlement and transaction history |
| analytics-worker | 3003 | Completed-payment analytics |

## Build

Prerequisites: JDK 21 and Maven 3.9+.

```text
mvn clean verify
```

Each service can also be built from its own directory with `mvn clean verify`.

## Health

Each service exposes Actuator health at `/health` and readiness at `/ready`.

## Development Status

`mvn clean verify` is the verified local build gate. Docker Compose configuration parsing is verified. Kubernetes runtime/API is verified locally: all infrastructure and application pods reached readiness, gateway health/readiness returned HTTP 200, a payment completed through Kafka and the ledger, insufficient funds produced a FAILED transaction, Redis idempotency and TTL were observed, and analytics/history endpoints returned data. The 250 TPS benchmark, 1M transaction run, and packet capture remain unverified.
