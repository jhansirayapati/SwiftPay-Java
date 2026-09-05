FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl transaction-gateway -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/transaction-gateway/target/transaction-gateway-0.1.0-SNAPSHOT.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
