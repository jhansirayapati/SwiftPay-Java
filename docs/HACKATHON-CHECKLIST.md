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
| HTTP status and error response | IMPLEMENTED - VERIFIED |
| Kafka retry and DLQ | IMPLEMENTED - VERIFIED |
| Health and readiness | VERIFIED |
| Correlation logging | IMPLEMENTED - VERIFIED |
| Docker build | IMPLEMENTED -  VERIFIED |
| Docker runtime | IMPLEMENTED -  VERIFIED |
| Kubernetes manifests | VERIFIED |
| Kubernetes runtime | VERIFIED |
| GitHub Actions configuration | IMPLEMENTED -  VERIFIED |
| GitHub Actions remote execution |  EXECUTED - ENVIRONMENT LIMITATION |
| Unit tests | VERIFIED |
| Integration tests | VERIFIED for gateway MVC slice and live Kubernetes flow |
| Testcontainers | IMPLEMENTED |
| k6 load test | IMPLEMENTED -  VERIFIED; script inspection VERIFIED |
| 250 TPS target | EXECUTED - ENVIRONMENT LIMITATION |
| 1M transaction test | Partially done |
| PCAP |  EXECUTED - ENVIRONMENT LIMITATION |
| Documentation | VERIFIED |
| AI Prompting Playbook | VERIFIED |