FROM maven:3.9.9-eclipse-temurin-27 AS build

WORKDIR /workspace
COPY . .

ARG MODULE
ARG MODULE_DIR
RUN mvn -B -f pom.xml -pl "$MODULE" -am -DskipTests package \
    && mkdir -p /app \
    && find "$MODULE_DIR/target" -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' -exec cp {} /app/app.jar \;

FROM eclipse-temurin:27-alpine

WORKDIR /app
COPY --from=build /app/app.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
