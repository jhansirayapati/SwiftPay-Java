# PCAP Capture Procedure

A network packet capture was successfully executed against the SwiftPay
transaction gateway during final validation.

## Capture Method

Windows Packet Monitor (`pktmon`) was used because the validation environment
was running on Windows with Docker Desktop/Kubernetes.

### Capture Configuration

- Capture tool: Windows `pktmon`
- Filter: TCP port `3101`
- Capture type: All packets
- Packet size: Unlimited (`--pkt-size 0`)
- Logging mode: Circular
- Maximum ETL size: 512 MB
- Capture file: `evidence/swiftpay-payment.etl`

The capture was configured using:

```powershell
pktmon filter remove
pktmon filter add -p 3101
The capture was started using:

pktmon start --capture --pkt-size 0 --file-name "C:\Users\King Of Lenovo\Desktop\Swiftpay Java\evidence\swiftpay-payment.etl"

PktMon reported:

Logger name:        PktMon
Logging mode:       Circular
Log file:           C:\Users\King Of Lenovo\Desktop\Swiftpay Java\evidence\swiftpay-payment.etl
Max file size:      512 MB
Memory used:        256 MB
Capture Type:       All packets

Packet Filters:
 # Name    Port
 1 <empty> 3101
Test Request

A real SwiftPay payment request was generated while the packet capture was
active.

Request
POST http://127.0.0.1:3101/v1/payments
Content-Type: application/json
Accept: */*
Request Payload
{
  "transaction_id": "txn-pcap-003",
  "sender_id": "user_001",
  "receiver_id": "user_002",
  "amount": 1,
  "currency": "INR",
  "description": "PCAP network trace test"
}

The request was sent using:

curl.exe -i -X POST "http://127.0.0.1:3101/v1/payments" `
  -H "accept: */*" `
  -H "Content-Type: application/json" `
  --data-binary "@.\evidence\pcap-payment.json"
Response

The gateway successfully accepted the payment request.

HTTP/1.1 202
Content-Type: application/json
Response Body
{
  "transactionId": "txn-pcap-003",
  "status": "PENDING",
  "senderId": "user_001",
  "receiverId": "user_002",
  "amount": "1",
  "currency": "INR"
}
Correlation ID
4bb933e0-8fb5-4660-b710-80931ffd6e3f

The request therefore produced the expected HTTP 202 Accepted response while
the packet capture was active.
