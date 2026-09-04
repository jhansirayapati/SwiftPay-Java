# SwiftPay API

## Endpoints

- `POST /v1/payments` accepts `{transaction_id,sender_id,receiver_id,amount,currency,description}` and returns `202` with `PENDING`, including `transactionId`, `senderId`, `receiverId`, string-safe `amount`, and `createdAt`.
- `GET /v1/users/{userId}/transactions?page=1&limit=20&status=COMPLETED` returns the sender's paged transaction history. Page numbering is one-based.
- `GET /v1/analytics/volume` returns a decimal aggregate.

- `GET /health`
- `GET /health/readiness` is the Actuator readiness group endpoint when probes are enabled.

Errors contain `timestamp`, `code`, `message`, and `correlationId`.
