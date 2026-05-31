# Estágio de Compilação (Build)
# Usa a imagem JDK 25 com Alpine Linux
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copia os ficheiros necessários para o Maven Wrapper e o código fonte
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src

# Dá permissão de execução ao script mvnw e compila o projeto
RUN apk add --no-cache dos2unix && dos2unix mvnw && chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Estágio de Execução (Runtime)
# Usa a imagem JRE 25 (apenas o necessário para correr a app)
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

# Copia o .jar gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
