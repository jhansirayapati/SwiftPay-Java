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

**Not yet implemented:**

- Production secret management and authentication
- Full Testcontainers integration suite
- Operational execution of the distributed stack in this environment

Ledger service is the Flyway migration owner for the shared Compose database. Gateway and analytics disable their duplicate migration locations in Compose to avoid competing `V1` histories; their JPA mappings use the schema created by ledger-service. Kubernetes resources are split across `k8s/swiftpay.yaml`, `k8s/infrastructure.yaml`, and `k8s/applications.yaml`; runtime deployment was not executed.

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

`mvn clean verify` is the verified local build gate. Docker Compose configuration parsing has been verified with a temporary environment value. A Docker runtime attempt was made, but image build failed on a Docker Desktop containerd `input/output error` while reading the Kafka image blob; no containers started. A k6 smoke test was also attempted and recorded connection refusals because the gateway was unavailable. Kubernetes runtime and packet capture remain unverified.
