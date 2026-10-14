# ---------- Etapa 1: compilar el backend ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---------- Etapa 2: imagen liviana para ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Los JSON e imágenes se guardan aquí; en Railway se monta un volumen en /data
ENV DATA_DIR=/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
