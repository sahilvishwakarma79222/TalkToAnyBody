# Step 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .

# Build application
RUN mvn clean package -DskipTests

# Step 2: Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create data directory for H2 database
RUN mkdir -p /app/data

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables for production (fixed URL)
ENV DB_URL="jdbc:h2:file:/app/data/chatapp;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
ENV CACHE_ENABLED=true
ENV SQL_SHOW=false
ENV H2_CONSOLE=false
ENV LOG_LEVEL=INFO

# Run with dynamic port for Render
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT:-8080} app.jar"]