# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/pixel-flow-app/target/pixel-flow-app-0.0.1-SNAPSHOT.jar app.jar
ENV RABBITMQ_HOST=rabbitmq
ENV FILE_STORAGE_PATH=/data/pixelflow
ENTRYPOINT ["java", "-jar", "app.jar"]
