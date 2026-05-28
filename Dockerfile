# ==========================
# Build Stage
# ==========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -Dmaven.test.skip=true


# ==========================
# Run Stage
# ==========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S synapse && adduser -S synapse -G synapse

# Create logs directory and give permissions
RUN mkdir -p /app/logs && \
    chown -R synapse:synapse /app

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Give ownership to app files
RUN chown -R synapse:synapse /app

# Switch user
USER synapse

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]