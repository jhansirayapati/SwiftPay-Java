# SwiftPay AI Prompting Playbook

This records prompts actually used during the implementation and final validation work. It does not claim that external tools ran when they did not.

## Actual prompts and workflow

* **Initial architecture/scaffolding prompt**: the supplied Phase 1 request for a Java 21 Spring Boot multi-module skeleton.

* **Implementation prompt**: the supplied request to implement the complete SwiftPay gateway, ledger, analytics, persistence, Kafka, Redis, deployment, testing, and documentation stack incrementally.

* **Corrective prompt**: the focused request to fix snake_case API contracts, after-commit side effects, event metadata, and transaction history behavior.

* **Validation prompt**: the supplied final runtime, E2E, performance, Kubernetes, PCAP, and submission audit request.

* **Debugging work**: investigation of failing YAML context tests, followed by conversion of invalid flow-style mappings to block-style YAML.

* **Review work**: inspection of migration ownership, which identified the shared-database Flyway `V1` conflict and led to ledger-only Flyway ownership in Compose.

* **Runtime work**: Docker Desktop and Kubernetes were recovered and re-probed successfully. Kubernetes services were brought to a working state and the distributed payment flow was validated.

* **Test coverage work**: the implementation workflow added Mockito tests for gateway idempotency and ledger settlement, plus a MockMvc gateway integration slice for snake_case request binding, accepted responses, and validation errors. The resulting Maven suite passed with 11 tests.

* **Kubernetes E2E validation**: a live payment flow was executed through the Kubernetes gateway. The payment was accepted with `202`, subsequently settled by the ledger, balances were updated correctly, Kafka initiated/completed events were observed, analytics processing was verified, Redis idempotency state was checked, insufficient-funds behavior was validated, and transaction history was confirmed.

* **Performance workflow**: the k6 script was inspected and extended with explicit `TARGET_TPS` and `TOTAL_REQUESTS` benchmark configuration.

* **250 TPS benchmark — 7,500 requests**: the benchmark was executed with 300 VUs, a target of 250 TPS, and 7,500 total payment requests. The run completed with 0% HTTP failures and 0 dropped iterations. Observed throughput was approximately 249.7 TPS and p95 latency was 402.95 ms. The strict throughput and p99 thresholds were marginally not met, while the p95 latency target was met.

* **25K / 250 TPS benchmark**: an extended benchmark was subsequently executed with a target of 250 TPS and 25,000 total requests. The run completed all 25,000 iterations in 1 minute 40 seconds with 0 interrupted iterations and a reported execution rate of 250.00 iterations/sec. k6 reported that the `http_reqs` threshold was crossed; therefore, the result is recorded as a completed 25K workload at the target execution rate, while the strict k6 threshold result is retained separately rather than being represented as a fully passing threshold run.

* **Higher-scale performance validation**: an earlier 10,000-request / 250 TPS attempt was started but failed under the available local runtime, with 600 requests completed and 100% HTTP failures before the run stopped. This result is retained as an earlier runtime limitation and is not used as successful performance evidence.

* **1M-scale validation**: the benchmark configuration and load-test preparation were completed, but a full 1M-request execution has not yet been completed. Therefore, no 1M-request throughput, latency, or success-rate result is claimed.

* **PCAP validation**: a real successful payment request was captured using Windows PktMon on gateway port `3101`. The capture completed with no PktMon events lost. The resulting ETL was converted successfully to PCAPNG, producing `swiftpay-payment.pcapng` containing 72,810 formatted packets. The PCAP evidence is retained under the repository `evidence` directory.

## Prompting principles

Use prompts that name the service, contract, invariant, and validation command. Examples:

* "Change gateway idempotency without allowing balance mutation; add a focused test and run `mvn -pl transaction-gateway test`."

* "Review ledger settlement for duplicate Kafka delivery, lock ordering, NUMERIC precision, and negative balances; report file and line evidence."

* "Add an integration test using Testcontainers, but keep context tests passing when Docker is unavailable."

* "Do not claim load, Kubernetes, or packet-capture results unless the command was executed; record limitations explicitly."

Prefer explicit BigDecimal values and typed JSON event fields. Treat PostgreSQL as the source of truth and Redis as a disposable cache.

## Human validation

The repository was inspected and source diagnostics were checked. Maven validation was performed using Java 21, and the Maven test suite passed with 11 tests.

Compose configuration was parsed and runtime configuration was reviewed. Docker Desktop and Kubernetes were recovered successfully after runtime issues encountered during validation.

A live Kubernetes end-to-end payment flow was executed and validated across the gateway, Kafka, ledger, PostgreSQL, Redis, and analytics components. Successful settlement, balance mutation, event processing, idempotency behavior, insufficient-funds handling, and transaction history were verified.

The k6 performance workload was validated in smoke mode and then used for the executed 250 TPS benchmarks.

The initial 7,500-request benchmark completed with 0% HTTP failures and 0 dropped iterations, achieving approximately 249.7 TPS with p95 latency of 402.95 ms.

A subsequent 25,000-request benchmark completed all 25,000 iterations at a reported 250.00 iterations/sec over 1 minute 40 seconds, with 0 interrupted iterations. k6 reported that the strict `http_reqs` threshold was crossed, so the threshold result is not represented as a completely passing k6 threshold run.

An earlier 10,000-request attempt was unsuccessful and is retained as a historical runtime limitation. The 1M-request benchmark has been prepared but has not yet been completed, so no 1M performance result is claimed.

PCAP capture was completed using Windows PktMon. The capture reported no events lost during collection and was converted to a valid PCAPNG file containing 72,810 formatted packets.

## AI-Assisted Development

The project may use AI assistance for project scaffolding, architecture assistance, code generation, debugging, test generation, documentation, code review, and performance analysis. Human review and executable validation remain required.

AI assistance was used as an implementation and review aid, while runtime claims were based on commands and observable outputs actually produced during validation.

## Key learnings

* Keep the initial repository independently buildable before introducing distributed-system behavior.

* Record implementation and verification separately.

* Never describe an unexecuted benchmark, Kubernetes flow, or packet capture as completed evidence.

* Record failed or incomplete validation explicitly rather than replacing it with an inferred success.

* Use focused prompts that identify the service, invariant, expected behavior, and executable validation command.

* Validate distributed behavior across the complete path rather than relying only on unit tests.

* Treat PostgreSQL as the source of truth and Redis as a disposable cache.

* Use explicit decimal types such as `BigDecimal` for monetary values and typed event metadata for Kafka messages.

* Keep performance evidence separate from functional E2E evidence so that each claim can be traced to a specific executed command and result.

* Distinguish between **target throughput**, **completed workload**, and **k6 threshold status** when reporting performance results.

* A completed 25K workload at the requested execution rate provides stronger sustained-load evidence than a short benchmark, while the 1M benchmark must still be executed separately before claiming 1M-scale validation.
