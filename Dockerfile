FROM maven:3.9.9-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY . .

ARG MODULE=microservices/socio-service
ARG MODULE_DIR=microservices/socio-service

RUN mvn -B -f pom.xml -pl "${MODULE}" -am -DskipTests package \
    && JAR_FILE="$(find "${MODULE_DIR}/target" -maxdepth 1 -type f -name '*.jar' \
        ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*.original' -print -quit)" \
    && test -n "${JAR_FILE}" \
    && mkdir -p /app \
    && cp "${JAR_FILE}" /app/app.jar

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app
COPY --from=build /app/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
