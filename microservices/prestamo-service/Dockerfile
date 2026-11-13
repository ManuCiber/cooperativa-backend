FROM maven:3.9.9-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B -f pom.xml -pl microservices/prestamo-service -am -DskipTests package

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app
COPY --from=build /workspace/microservices/prestamo-service/target/prestamo-service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
