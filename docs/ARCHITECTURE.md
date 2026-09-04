# SwiftPay Architecture

Phase 1 establishes three independently deployable Spring Boot services. The gateway will accept payment requests, the ledger will own financial settlement, and the analytics worker will consume completed-payment events in later phases.

The gateway owns intake and idempotency but never changes balances. Kafka carries typed JSON strings from `swiftpay.payment.initiated` to the ledger. The ledger is the financial authority: it locks sender/receiver rows in sorted order, applies BigDecimal NUMERIC changes, and emits metadata-rich completed or failed events after the database commit. Analytics consumes completed events with a unique transaction constraint.

All services use environment-driven endpoints. Default H2 and disabled Flyway permit context tests without infrastructure; Compose enables PostgreSQL and Flyway.
