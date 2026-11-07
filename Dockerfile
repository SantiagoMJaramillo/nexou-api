# ---- Etapa 1: compilar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package && cp target/*.jar app.jar

# ---- Etapa 2: ejecutar ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
# Render inyecta PORT; la memoria se limita porque el plan gratuito tiene 512 MB
ENV JAVA_OPTS="-Xmx350m -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
