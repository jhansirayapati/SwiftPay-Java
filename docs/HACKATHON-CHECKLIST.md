# Hackathon Checklist

Use only these statuses: `VERIFIED`, `IMPLEMENTED - NOT VERIFIED`, `PARTIALLY IMPLEMENTED`, `NOT IMPLEMENTED`, and `NOT EXECUTED - ENVIRONMENT LIMITATION`.

| Requirement | Status |
| --- | --- |
| POST `/v1/payments` | VERIFIED |
| Redis idempotency | VERIFIED |
| PENDING state | VERIFIED |
| Kafka PaymentInitiated | VERIFIED |
| Atomic debit/credit | VERIFIED |
| Insufficient funds | VERIFIED |
| PaymentCompleted / PaymentFailed | VERIFIED |
| Transaction history | VERIFIED |
| Analytics | VERIFIED |
| Swagger/OpenAPI | VERIFIED |
| HTTP status and error response | IMPLEMENTED - NOT VERIFIED |
| Kafka retry and DLQ | IMPLEMENTED - NOT VERIFIED |
| Health and readiness | VERIFIED |
| Correlation logging | IMPLEMENTED - NOT VERIFIED |
| Docker build | IMPLEMENTED - NOT VERIFIED |
| Docker runtime | IMPLEMENTED - NOT VERIFIED |
| Kubernetes manifests | VERIFIED |
| Kubernetes runtime | VERIFIED |
| GitHub Actions configuration | IMPLEMENTED - NOT VERIFIED |
| GitHub Actions remote execution | NOT EXECUTED - ENVIRONMENT LIMITATION |
| Unit tests | VERIFIED |
| Integration tests | VERIFIED for gateway MVC slice and live Kubernetes flow |
| Testcontainers | NOT IMPLEMENTED |
| k6 load test | IMPLEMENTED - NOT VERIFIED; script inspection VERIFIED |
| 250 TPS target | NOT EXECUTED - ENVIRONMENT LIMITATION |
| 1M transaction test | NOT EXECUTED - ENVIRONMENT LIMITATION |
| PCAP | NOT EXECUTED - ENVIRONMENT LIMITATION |
| Documentation | VERIFIED |
| AI Prompting Playbook | VERIFIED |