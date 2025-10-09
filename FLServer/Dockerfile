#FROM ubuntu:latest
#LABEL authors="User"
#
#ENTRYPOINT ["top", "-b"]

#FROM ubuntu:latest
#LABEL authors="User"
#
#ENTRYPOINT ["top", "-b"]

# Step 1: Build the application using Maven
FROM maven:3.9.2-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy all project files (including pom.xml) to the container
COPY . .

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Step 2: Use a lightweight JRE image for production
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (change if needed)
EXPOSE 8086

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
