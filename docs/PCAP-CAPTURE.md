# PCAP Capture Procedure

No capture has been executed or claimed in this repository.

## tcpdump

1. Start the stack with `docker compose --env-file .env up --build`.
2. Identify the Docker bridge interface with `docker network ls` and the host interface details for that network.
3. Start capture on the relevant interface, for example:

	`sudo tcpdump -i <interface> -w swiftpay.pcapng host <gateway-host> or port 5432 or port 6379 or port 9092`

4. Run the k6 script from `k6/load-tests/payment-load.js`.
5. Stop tcpdump with Ctrl+C after the test completes.
6. Inspect `swiftpay.pcapng` in Wireshark and confirm HTTP gateway traffic plus the expected PostgreSQL, Redis, and Kafka connections.
7. Add the resulting PCAP only if repository size and data-handling policy permit it. Redact or exclude credentials and other sensitive data.

## Wireshark

1. Start the Compose stack and k6 test.
2. Select the Docker bridge or host interface carrying the traffic.
3. Use a display filter such as `tcp.port == 3001 || tcp.port == 5432 || tcp.port == 6379 || tcp.port == 9092`.
4. Start and stop the capture around the test, then save it as `swiftpay.pcapng`.
5. Verify the saved file opens and contains the expected flows. Record the capture date, interface, filters, and test configuration alongside the artifact.

The resulting file is intentionally absent until an actual capture is performed.