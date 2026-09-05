# SwiftPay Performance

## Status

**Target not executed.**

The k6 script was syntax-inspected successfully. An earlier smoke command was executed with 1 VU for 1 second, but the gateway was unavailable because Compose had not started. It produced 2,098 refused requests, 100% request failure, and no meaningful latency measurement. This is not a throughput benchmark.

The required 250 TPS and 1,000,000 transaction tests remain **NOT EXECUTED - ENVIRONMENT LIMITATION**. Run `k6 run k6/load-tests/payment-load.js` only against a running Compose stack, then record raw output and environment here.

The script supports a dedicated benchmark mode without running during normal CI:

`$env:BASE_URL="http://localhost:3001"; $env:TARGET_TPS="250"; $env:TOTAL_REQUESTS="1000000"; $env:VUS="250"; k6 run k6/load-tests/payment-load.js`

At 250 TPS, one million requests require 4,000 seconds, approximately 66 minutes and 40 seconds.
