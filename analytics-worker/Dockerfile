FROM maven:3.9.15-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl analytics-worker -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/analytics-worker/target/analytics-worker-0.1.0-SNAPSHOT.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
