FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

RUN apk add --no-cache dos2unix && dos2unix mvnw && chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
