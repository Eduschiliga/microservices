# AGENTS.md

## Project Context

This repository is a Java microservices example for user management and authentication.
All external HTTP traffic should enter through the API Gateway, which routes requests to services registered in Eureka.
Orders are created synchronously through the `order` service and payments are processed asynchronously by the `payment` service through Kafka.

Core stack:

- Java 25 with Amazon Corretto.
- Maven 3.9.x.
- Spring Boot 4.x.
- Spring Cloud 2025.1.x.
- Spring Cloud Netflix Eureka.
- Spring Cloud Gateway WebFlux.
- Spring Security with JWT using Auth0 `java-jwt`.
- Spring MVC, Spring Data JPA, Hibernate, PostgreSQL, MapStruct, OpenAPI Generator.
- Spring Kafka for asynchronous order payment processing.
- Transactional outbox for order/payment Kafka publications.
- Resilience4j for retry, circuit breaker, and fallback behavior around payment processing.
- Docker Compose for local orchestration.

Read these supporting docs when a task touches the relevant area:

- `docs/codex/project-context.md` for architecture, service topology, ports, and request flow.
- `docs/codex/services.md` for service responsibilities, package layout, routes, security, and roles.
- `docs/codex/development.md` for local setup, Maven/Docker commands, test notes, and final checklist.
- `docs/maintenance-guide.md` for the complete developer maintenance guide.

## Repository Rules

- Keep changes scoped to the service or layer involved in the request.
- Respect the `user` service hexagonal architecture:
  - domain rules stay under `application/domain`;
  - inbound/outbound contracts stay under `application/ports`;
  - business orchestration stays under `application/usecases`;
  - frameworks, controllers, repositories, security filters, and adapters stay under `infrastructure`.
- The `user` service is OpenAPI-first. Do not change API behavior or DTO shape without updating `user/src/main/resources/api/openapi.yml`, generated-contract usage, implementation, and tests together.
- The `gateway` validates JWT locally using the same `JWT_SECRET` as `user`, while `user` keeps its own Spring Security validation and method-level authorization.
- Do not remove service-level security from `user` just because the gateway performs authentication.
- Prefer existing Spring, Maven, and package patterns over adding new abstractions.

## Common Commands

- Run all services locally: `docker compose up --build`.
- Run tests for one service from that service directory: `mvn test`.
- Build one service from that service directory: `mvn clean package`.
- For gateway-specific validation: `cd gateway && mvn test`.
- For user-specific validation: `cd user && mvn test`.
- For order-specific validation: `cd order && mvn test`.
- For payment-specific validation: `cd payment && mvn test`.
- For eureka-specific validation: `cd eureka && mvn test`.

## Verification Expectations

- Run the narrowest relevant Maven test command after code changes.
- For gateway authentication changes, include or update tests around public endpoints, missing token, invalid token, and valid token.
- For user use-case changes, prefer focused unit tests in `user/src/test/java/br/com/fiap/user/application/usecases`.
- For order/payment changes, keep domain and use-case logic in the application layer and Kafka/JPA details in infrastructure adapters.
- For order/payment Kafka publications, preserve the transactional outbox flow: use cases call messaging ports, outbox adapters persist events, and scheduled infrastructure publishers deliver pending rows to Kafka.
- For Docker or service-discovery changes, verify `docker compose up --build` when practical.
- Mention any skipped verification and why.
