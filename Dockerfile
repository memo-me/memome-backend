FROM eclipse-temurin:21-jdk-resolute
LABEL authors="soyounglee"

ARG DEPENDENCY=./build/dependency

WORKDIR /app
RUN addgroup --system spring && \
    adduser --system --ingroup spring spring
USER spring:spring

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","/app:/app/lib/*", "cloud.memome.backend.MemomeBackendApplication", "--spring.profiles.active=prod"]