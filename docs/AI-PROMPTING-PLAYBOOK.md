# SwiftPay AI Prompting Playbook

This records prompts actually used during the implementation and final validation work. It does not claim that external tools ran when they did not.

## Actual prompts and workflow

- Initial architecture/scaffolding prompt: the supplied Phase 1 request for a Java 21 Spring Boot multi-module skeleton.
- Implementation prompt: the supplied request to implement the complete SwiftPay gateway, ledger, analytics, persistence, Kafka, Redis, deployment, testing, and documentation stack incrementally.
- Corrective prompt: the focused request to fix snake_case API contracts, after-commit side effects, event metadata, and transaction history behavior.
- Validation prompt: the supplied final runtime, E2E, performance, Kubernetes, CI, PCAP, and submission audit request.
- Debugging work: investigation of failing YAML context tests, followed by conversion of invalid flow-style mappings to block-style YAML.
- Review work: inspection of migration ownership, which identified the shared-database Flyway `V1` conflict and led to ledger-only Flyway ownership in Compose.
- Final runtime work: Docker Desktop was re-probed successfully, Compose image build was attempted, and the actual containerd Kafka image-blob `input/output error` was recorded. k6 `v2.2.0` was run for a one-second smoke test and recorded connection refusals because no gateway container was available.

## Prompting principles

Use prompts that name the service, contract, invariant, and validation command. Examples:

- "Change gateway idempotency without allowing balance mutation; add a focused test and run `mvn -pl transaction-gateway test`."
- "Review ledger settlement for duplicate Kafka delivery, lock ordering, NUMERIC precision, and negative balances; report file and line evidence."
- "Add an integration test using Testcontainers, but keep context tests passing when Docker is unavailable."
- "Do not claim load, Kubernetes, or packet-capture results unless the command was executed; record limitations explicitly."

Prefer explicit BigDecimal values and typed JSON event fields. Treat PostgreSQL as the source of truth and Redis as a disposable cache.
## Human validation

The repository was inspected, Maven was run with Java 21, Compose configuration was parsed, and source diagnostics were checked. Docker runtime was attempted but failed during image-layer reading; Kubernetes runtime and PCAP were unavailable. k6 was available and executed only as an unsuccessful one-second smoke test against an unavailable gateway. No live distributed E2E flow is claimed.

## 2. AI-Assisted Development

The project may use AI assistance for project scaffolding, architecture assistance, code generation, debugging, test generation, documentation, code review, and performance analysis. Human review and executable validation remain required.

## Key learnings

- Keep the initial repository independently buildable before introducing distributed-system behavior.
- Record implementation and verification separately.
