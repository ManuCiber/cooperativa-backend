FROM maven:3.9.9-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B -f pom.xml -pl microservices/configuration-service -am -DskipTests package

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app
COPY --from=build /workspace/microservices/configuration-service/target/configuration-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
