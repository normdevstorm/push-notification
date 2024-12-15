# Use a Maven image to build the project
FROM maven:3.9.3-eclipse-temurin-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Use a smaller JRE image to run the application
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/push-notification-0.0.1-SNAPSHOT.jar app.jar

# Use a non-root user for security (optional)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
