# Development

## Local Environment

Expected local tools:

- Amazon Corretto JDK 25.
- Maven 3.9.16 or compatible Maven 3.9.x.
- Docker and Docker Compose.

Useful checks:

```powershell
java -version
javac -version
mvn -version
docker compose version
```

The project `pom.xml` files declare Java 25. If Maven fails with `release version 25 not supported`, `JAVA_HOME` is pointing to an older JDK.

## Maven Commands

Run Maven from the module directory being changed:

```powershell
cd gateway
mvn test
```

```powershell
cd user
mvn test
```

```powershell
cd order
mvn test
```

```powershell
cd payment
mvn test
```

```powershell
cd eureka
mvn test
```

Build a module:

```powershell
mvn clean package
```

The `user` module uses OpenAPI Generator during Maven build. Generated sources are expected under `target/generated-sources`.

## Docker Compose Commands

Start everything:

```powershell
docker compose up --build
```

Check services:

```powershell
docker compose ps
```

Stop services:

```powershell
docker compose down
```

Important local URLs:

- Eureka dashboard: `http://localhost:8761`.
- Gateway health: `http://localhost:9090/actuator/health`.
- User health: `http://localhost:8080/actuator/health`.
- Order health: `http://localhost:8081/actuator/health`.
- Payment health: `http://localhost:8082/actuator/health`.
- Kafka host bootstrap server: `localhost:29092`.

Full maintenance documentation for future developers lives in `docs/maintenance-guide.md`.

## Test Notes

- `gateway` tests may log connection warnings if Eureka is not running. These warnings are acceptable when the Maven build still ends with `BUILD SUCCESS`.
- Gateway authentication tests should cover public login, public user creation, protected route without token, protected route with invalid token, and protected route with valid token.
- User use-case tests should stay focused on application behavior and avoid requiring a running database unless the task explicitly needs integration coverage.
- Order tests should verify order creation stores a payment request outbox event, the outbox publisher sends it to Kafka, and payment result handling updates status.
- Payment tests should verify approval, fallback failure, result outbox storage, and outbox publication to Kafka.
- Outbox integration tests use Testcontainers with real PostgreSQL and Kafka containers; they require Docker to be running and can take longer than pure unit tests.
- For controller/API changes in `user`, confirm the OpenAPI contract, generated interfaces, mapper behavior, and security annotations remain aligned.

## Final Checklist for Codex

Before finishing a code change:

- Confirm the changed service and layer match the request.
- Run the narrowest relevant `mvn test` command.
- For route/security changes, verify public and protected behavior.
- For API contract changes, update OpenAPI, implementation, and tests together.
- For Kafka/outbox changes, verify both the database write and the asynchronous publication path.
- For Docker or environment changes, mention whether `docker compose up --build` was run.
- Report any warnings that are expected, such as Eureka connection warnings during isolated gateway tests.
