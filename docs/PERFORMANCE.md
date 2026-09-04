# SwiftPay Performance

## Status

**Target not executed.**

The k6 smoke command was executed with 1 VU for 1 second, but the gateway was unavailable because Compose had not started. It produced 2,098 refused requests, 100% request failure, and no meaningful latency measurement. This is not a throughput benchmark.

The required 250 TPS and 1,000,000 transaction tests remain **NOT EXECUTED - ENVIRONMENT LIMITATION**. Run `k6 run k6/load-tests/payment-load.js` only against a running Compose stack, then record raw output and environment here.
