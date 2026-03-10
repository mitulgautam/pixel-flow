# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/watermark-image-processing/target/watermark-image-processing-0.0.1-SNAPSHOT.jar app.jar
# Copy the logo to a known path in the container
COPY --from=build /app/pixel-flow-app/src/main/resources/wmc-logo.png /app/resources/wmc-logo.png
ENV RABBITMQ_HOST=rabbitmq
ENV LOGO_PATH=/app/resources/wmc-logo.png
ENTRYPOINT ["java", "-jar", "app.jar"]
