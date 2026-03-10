# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/sizing-image-processing/target/sizing-image-processing-0.0.1-SNAPSHOT.jar app.jar
ENV RABBITMQ_HOST=rabbitmq
# The worker needs to run the main class explicitly as it might not be set in the JAR manifest correctly
ENTRYPOINT ["java", "-jar", "app.jar"]
