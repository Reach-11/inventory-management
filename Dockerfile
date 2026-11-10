# syntax=docker/dockerfile:1

# ======================= Stage 1: BUILD =======================
# Full JDK 25 image: contains the compiler we need to build the app.
FROM eclipse-temurin:25-jdk AS build

# All following commands run inside /workspace (created automatically).
WORKDIR /workspace

# Copy ONLY the files that describe dependencies first. Docker caches each
# instruction as a "layer"; as long as these files don't change, the
# dependency download below is reused and builds stay fast.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

# Warm the dependency cache. "|| true": a failure here must not stop the build;
# the real build step below will show any genuine error.
RUN chmod +x gradlew && ./gradlew --no-daemon --quiet dependencies > /dev/null 2>&1 || true

# Now copy the source code (changes often, so it comes AFTER the dependency layer).
COPY src src

# Build the executable Spring Boot JAR -> /workspace/build/libs/app.jar
# Tests already ran in the Jenkins pipeline, so skip them here.
RUN ./gradlew --no-daemon bootJar -x test

# ======================= Stage 2: RUNTIME =======================
# Small JRE-only image (no compiler, no Gradle, no source code).
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create an unprivileged user. If the app is ever hacked, the attacker is not root.
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring

# Copy only the finished JAR from the build stage.
COPY --from=build /workspace/build/libs/app.jar app.jar

# Switch to the unprivileged user for everything from here on.
USER spring

# Documentation: the app listens on 8080 inside the container.
EXPOSE 8080

# Exec form (JSON array) so Java is PID 1 and receives stop signals correctly.
# MaxRAMPercentage lets the JVM size its heap from the container's memory limit.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
