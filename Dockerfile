FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
ARG SERVICE
RUN mvn -pl ${SERVICE} -am -DskipTests package
FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY --from=build /src/${SERVICE}/target/${SERVICE}-0.1.0-SNAPSHOT.jar app.jar
USER 10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
