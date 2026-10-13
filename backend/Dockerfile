# Multi-stage: build with Maven+JDK, run on a slim JRE only.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Cache dependency downloads separately from source changes.
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S solaris && adduser -S solaris -G solaris
COPY --from=build /build/target/memory-assistant-*.jar app.jar
USER solaris
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
