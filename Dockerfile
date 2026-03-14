# This Dockerfile is placed in the project root to satisfy cloud providers 
# that don't support changing the "Root Directory" for deployment.
# It automatically targets the backend-springboot source files.

FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the backend code into the container
COPY backend-springboot/pom.xml .
COPY backend-springboot/src ./src

# Package the application
RUN mvn clean package -DskipTests

# Use smaller JDK image to run app
FROM eclipse-temurin:21-jdk

WORKDIR /app
# Extract the built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java","-jar","app.jar"]
