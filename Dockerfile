# Étape 1 : Build de l'application
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Étape 2 : Exécution
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Port par défaut de Render
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
