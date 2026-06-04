# --- Build stage: compile the Spring Boot jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven wrapper + project descriptor first so Docker can cache deps
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Now copy source and build
COPY src src
RUN ./mvnw -B clean package -Dmaven.test.skip=true

# --- Runtime stage: only the JRE + the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Render injects PORT at runtime; Spring reads it via application.properties.
# Use shell form so env var expansion works for the Mongo URI override.
# Command-line args are the HIGHEST priority property source in Spring,
# so this absolutely overrides anything from application.properties.
ENTRYPOINT ["/bin/sh", "-c", "exec java -jar /app/app.jar --spring.data.mongodb.uri=\"$SPRING_DATA_MONGODB_URI\""]
