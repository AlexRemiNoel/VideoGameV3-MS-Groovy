FROM gradle:7.5-jdk17 AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src


RUN chmod +x ./gradlew

# Build without daemon and parallelization to avoid permission issues
RUN ./gradlew bootJar --no-daemon --no-parallel --debug

FROM openjdk:21-jdk-slim
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
