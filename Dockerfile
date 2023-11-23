# Build stage
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ /app/src/
RUN mvn package -DskipTests

# Production stage
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/s3rekognition-0.0.1-SNAPSHOT.jar /app
ENV AWS_REGION="eu-west-1"
CMD ["java", "-jar", "s3rekognition-0.0.1-SNAPSHOT.jar"]