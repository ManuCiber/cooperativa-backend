FROM maven:3.9.9-eclipse-temurin-24 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B -f pom.xml -pl common-web -am -DskipTests package

FROM scratch
LABEL org.opencontainers.image.title="common-web build artifact"
COPY --from=build /workspace/common-web/target/common-web-0.0.1-SNAPSHOT.jar /common-web.jar
