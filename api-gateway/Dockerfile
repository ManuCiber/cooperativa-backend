FROM maven:3.9.9-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B -f pom.xml -pl api-gateway -am -DskipTests package

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
