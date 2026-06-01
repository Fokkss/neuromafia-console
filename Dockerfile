FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean installDist --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/install/neuromafia ./neuromafia

ENTRYPOINT ["./neuromafia/bin/neuromafia"]
CMD ["--help"]