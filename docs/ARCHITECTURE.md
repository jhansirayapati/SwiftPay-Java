# SwiftPay Architecture

Phase 1 establishes three independently deployable Spring Boot services. The gateway will accept payment requests, the ledger will own financial settlement, and the analytics worker will consume completed-payment events in later phases.

The gateway owns intake and idempotency but never changes balances. Kafka carries typed JSON strings from `swiftpay.payment.initiated` to the ledger. The ledger is the financial authority: it locks sender/receiver rows in sorted order, applies BigDecimal NUMERIC changes, and emits metadata-rich completed or failed events after the database commit. Analytics consumes completed events with a unique transaction constraint.

All services use environment-driven endpoints. Default H2 and disabled Flyway permit context tests without infrastructure; Compose enables PostgreSQL and Flyway.
swiftpay Java
|   .dockerignore
|   .env
|   .env.example
|   .gitignore
|   docker-compose.yml
|   Dockerfile
|   ledger-app.jar
|   pom.xml
|   README.md
|   
+---.github
|   +---modernize
|   |   \---java-upgrade
|   |       |   .gitignore
|   |       |   
|   |       +---20260904112428
|   |       |   \---logs
|   |       |           0.log
|   |       |           
|   |       \---hooks
|   |           \---scripts
|   |                   recordToolUse.ps1
|   |                   recordToolUse.sh
|   |                   
|   \---workflows
|           ci.yml
|           
+---analytics-worker
|   |   Dockerfile
|   |   pom.xml
|   |   
|   +---src
|   |   +---main
|   |   |   +---java
|   |   |   |   \---com
|   |   |   |       \---swiftpay
|   |   |   |           \---analytics
|   |   |   |               |   AnalyticsWorkerApplication.java
|   |   |   |               |   
|   |   |   |               +---config
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---controller
|   |   |   |               |       AnalyticsController.java
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---dto
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---entity
|   |   |   |               |       package-info.java
|   |   |   |               |       PaymentAnalytics.java
|   |   |   |               |       
|   |   |   |               +---exception
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---kafka
|   |   |   |               |       AnalyticsListener.java
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---mapper
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---redis
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---repository
|   |   |   |               |       AnalyticsRepository.java
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               +---service
|   |   |   |               |       package-info.java
|   |   |   |               |       
|   |   |   |               \---validation
|   |   |   |                       package-info.java
|   |   |   |                       
|   |   |   \---resources
|   |   |       |   application.yml
|   |   |       |   
|   |   |       \---db
|   |   |           \---migration
|   |   |                   V1__analytics.sql
|   |   |                   
|   |   \---test
|   |       \---java
|   |           \---com
|   |               \---swiftpay
|   |                   \---analytics
|   |                           AnalyticsWorkerApplicationTests.java
|   |                           
|   \---target
|       |   analytics-worker-0.1.0-SNAPSHOT.jar
|       |   analytics-worker-0.1.0-SNAPSHOT.jar.original
|       |   
|       +---classes
|       |   |   application.yml
|       |   |   
|       |   +---com
|       |   |   \---swiftpay
|       |   |       \---analytics
|       |   |           |   AnalyticsWorkerApplication.class
|       |   |           |   
|       |   |           +---config
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---controller
|       |   |           |       AnalyticsController.class
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---dto
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---entity
|       |   |           |       package-info.class
|       |   |           |       PaymentAnalytics.class
|       |   |           |       
|       |   |           +---exception
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---kafka
|       |   |           |       AnalyticsListener$Event.class
|       |   |           |       AnalyticsListener.class
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---mapper
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---redis
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---repository
|       |   |           |       AnalyticsRepository.class
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           +---service
|       |   |           |       package-info.class
|       |   |           |       
|       |   |           \---validation
|       |   |                   package-info.class
|       |   |                   
|       |   \---db
|       |       \---migration
|       |               V1__analytics.sql
|       |               
|       +---generated-sources
|       |   \---annotations
|       +---generated-test-sources
|       |   \---test-annotations
|       +---maven-archiver
|       |       pom.properties
|       |       
|       +---maven-status
|       |   \---maven-compiler-plugin
|       |       +---compile
|       |       |   \---default-compile
|       |       |           createdFiles.lst
|       |       |           inputFiles.lst
|       |       |           
|       |       \---testCompile
|       |           \---default-testCompile
|       |                   createdFiles.lst
|       |                   inputFiles.lst
|       |                   
|       +---surefire-reports
|       |       com.swiftpay.analytics.AnalyticsWorkerApplicationTests.txt
|       |       TEST-com.swiftpay.analytics.AnalyticsWorkerApplicationTests.xml
|       |       
|       \---test-classes
|           \---com
|               \---swiftpay
|                   \---analytics
|                           AnalyticsWorkerApplicationTests.class
|                           
+---docs
|       AI-PROMPTING-PLAYBOOK.md
|       API.md
|       ARCHITECTURE.md
|       HACKATHON-CHECKLIST.md
|       PCAP-CAPTURE.md
|       PERFORMANCE.md
|       
+---evidence
|       pcap-payment.json
|       swiftpay-payment.etl
|       swiftpay-payment.pcapng
|       
+---jar-check
|       ledger-app.jar
|       
+---k6
|   \---load-tests
|           payment-load.js
|           
+---k8s
|       applications.yaml
|       infrastructure.yaml
|       swiftpay.yaml
|       
+---ledger-service
|   |   Dockerfile
|   |   pom.xml
|   |   
|   \---src
|       +---main
|       |   +---java
|       |   |   \---com
|       |   |       \---swiftpay
|       |   |           \---ledger
|       |   |               |   LedgerServiceApplication.java
|       |   |               |   
|       |   |               +---config
|       |   |               |       KafkaConfig.java
|       |   |               |       package-info.java
|       |   |               |       
|       |   |               +---controller
|       |   |               |       package-info.java
|       |   |               |       TransactionController.java
|       |   |               |       
|       |   |               +---dto
|       |   |               |       package-info.java
|       |   |               |       
|       |   |               +---entity
|       |   |               |       LedgerTransaction.java
|       |   |               |       package-info.java
|       |   |               |       UserAccount.java
|       |   |               |       
|       |   |               +---exception
|       |   |               |       package-info.java
|       |   |               |       
|       |   |               +---kafka
|       |   |               |       package-info.java
|       |   |               |       SettlementListener.java
|       |   |               |       
|       |   |               +---mapper
|       |   |               |       package-info.java
|       |   |               |       
|       |   |               +---repository
|       |   |               |       LedgerRepositories.java
|       |   |               |       LedgerTransactionRepository.java
|       |   |               |       package-info.java
|       |   |               |       UserAccountRepository.java
|       |   |               |       
|       |   |               \---service
|       |   |                       package-info.java
|       |   |                       SettlementService.java
|       |   |                       
|       |   \---resources
|       |       |   application.yml
|       |       |   
|       |       \---db
|       |           \---migration
|       |                   V1__ledger.sql
|       |                   
|       \---test
|           \---java
|               \---com
|                   \---swiftpay
|                       \---ledger
|                           |   LedgerServiceApplicationTests.java
|                           |   
|                           \---service
|                                   SettlementServiceTest.java
|                                   
+---performance
|   \---evidence
\---transaction-gateway
    |   Dockerfile
    |   pom.xml
    |   
    +---src
    |   +---main
    |   |   +---java
    |   |   |   \---com
    |   |   |       \---swiftpay
    |   |   |           \---gateway
    |   |   |               |   TransactionGatewayApplication.java
    |   |   |               |   
    |   |   |               +---config
    |   |   |               |       CorrelationIdFilter.java
    |   |   |               |       GatewayConfig.java
    |   |   |               |       package-info.java
    |   |   |               |       
    |   |   |               +---controller
    |   |   |               |       package-info.java
    |   |   |               |       PaymentController.java
    |   |   |               |       
    |   |   |               +---dto
    |   |   |               |       package-info.java
    |   |   |               |       PaymentRequest.java
    |   |   |               |       PaymentResponse.java
    |   |   |               |       
    |   |   |               +---entity
    |   |   |               |       PaymentTransaction.java
    |   |   |               |       
    |   |   |               +---exception
    |   |   |               |       ApiExceptionHandler.java
    |   |   |               |       package-info.java
    |   |   |               |       
    |   |   |               +---kafka
    |   |   |               |       package-info.java
    |   |   |               |       
    |   |   |               +---mapper
    |   |   |               |       package-info.java
    |   |   |               |       
    |   |   |               +---redis
    |   |   |               |       package-info.java
    |   |   |               |       
    |   |   |               +---repository
    |   |   |               |       package-info.java
    |   |   |               |       PaymentTransactionRepository.java
    |   |   |               |       
    |   |   |               +---service
    |   |   |               |       package-info.java
    |   |   |               |       PaymentService.java
    |   |   |               |       
    |   |   |               \---validation
    |   |   |                       package-info.java
    |   |   |                       
    |   |   \---resources
    |   |       |   application.yml
    |   |       |   
    |   |       \---db
    |   |           \---migration
    |   |                   V1__gateway.sql
    |   |                   
    |   \---test
    |       \---java
    |           \---com
    |               \---swiftpay
    |                   \---gateway
    |                       |   TransactionGatewayApplicationTests.java
    |                       |   
    |                       +---controller
    |                       |       PaymentControllerIntegrationTest.java
    |                       |       
    |                       \---service
    |                               PaymentServiceTest.java
    |                               
    \---target
        |   transaction-gateway-0.1.0-SNAPSHOT.jar
        |   transaction-gateway-0.1.0-SNAPSHOT.jar.original
        |   
        +---classes
        |   |   application.yml
        |   |   
        |   +---com
        |   |   \---swiftpay
        |   |       \---gateway
        |   |           |   TransactionGatewayApplication.class
        |   |           |   
        |   |           +---config
        |   |           |       CorrelationIdFilter.class
        |   |           |       GatewayConfig.class
        |   |           |       package-info.class
        |   |           |       
        |   |           +---controller
        |   |           |       package-info.class
        |   |           |       PaymentController.class
        |   |           |       
        |   |           +---dto
        |   |           |       package-info.class
        |   |           |       PaymentRequest.class
        |   |           |       PaymentResponse.class
        |   |           |       
        |   |           +---entity
        |   |           |       PaymentTransaction.class
        |   |           |       
        |   |           +---exception
        |   |           |       ApiExceptionHandler.class
        |   |           |       package-info.class
        |   |           |       
        |   |           +---kafka
        |   |           |       package-info.class
        |   |           |       
        |   |           +---mapper
        |   |           |       package-info.class
        |   |           |       
        |   |           +---redis
        |   |           |       package-info.class
        |   |           |       
        |   |           +---repository
        |   |           |       package-info.class
        |   |           |       PaymentTransactionRepository.class
        |   |           |       
        |   |           +---service
        |   |           |       package-info.class
        |   |           |       PaymentService$PaymentEvent.class
        |   |           |       PaymentService.class
        |   |           |       
        |   |           \---validation
        |   |                   package-info.class
        |   |                   
        |   \---db
        |       \---migration
        |               V1__gateway.sql
        |               
        +---generated-sources
        |   \---annotations
        +---generated-test-sources
        |   \---test-annotations
        +---maven-archiver
        |       pom.properties
        |       
        +---maven-status
        |   \---maven-compiler-plugin
        |       +---compile
        |       |   \---default-compile
        |       |           createdFiles.lst
        |       |           inputFiles.lst
        |       |           
        |       \---testCompile
        |           \---default-testCompile
        |                   createdFiles.lst
        |                   inputFiles.lst
        |                   
        +---surefire-reports
        |       com.swiftpay.gateway.controller.PaymentControllerIntegrationTest.txt
        |       com.swiftpay.gateway.service.PaymentServiceTest.txt
        |       com.swiftpay.gateway.TransactionGatewayApplicationTests.txt
        |       TEST-com.swiftpay.gateway.controller.PaymentControllerIntegrationTest.xml
        |       TEST-com.swiftpay.gateway.service.PaymentServiceTest.xml
        |       TEST-com.swiftpay.gateway.TransactionGatewayApplicationTests.xml
        |       
        \---test-classes
            \---com
                \---swiftpay
                    \---gateway
                        |   TransactionGatewayApplicationTests.class
                        |   
                        +---controller
                        |       PaymentControllerIntegrationTest.class
                        |       
                        \---service
                                PaymentServiceTest.class
                                
PS C:\Users\King Of Lenovo\Desktop\Swiftpay Java> 
