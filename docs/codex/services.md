# Services

## Eureka Service

Eureka is the service registry.

- Module: `eureka`.
- Main class: `br.com.schiliga.eureka.EurekaApplication`.
- Port: `8761`.
- Spring role: Eureka Server.
- Docker container: `eureka-service`.
- Health endpoint used by Compose: `http://localhost:8761/actuator/health`.

Keep Eureka simple. It should not contain business logic, gateway routing rules, or user authentication logic.

## Gateway Service

Gateway is the public HTTP entrypoint.

- Module: `gateway`.
- Main class: `br.com.schiliga.gateway.GatewayApplication`.
- Port: `9090`.
- Spring role: Spring Cloud Gateway WebFlux and Eureka client.
- Docker container: `gateway-service`.
- Route target for user APIs: `lb://user`.
- Route target for order APIs: `lb://order`.
- Route paths: `/api/v1/auth/**`, `/api/v1/users/**`, and `/api/v1/orders/**`.

Security behavior:

- Public endpoints:
  - `POST /api/v1/auth/login`.
  - `POST /api/v1/users`.
  - `OPTIONS /**`.
  - `/actuator/health/**`.
- Protected endpoints require `Authorization: Bearer <jwt>`.
- `/api/v1/orders/**` is protected by the gateway.
- The gateway validates JWT locally using Auth0 `java-jwt`.
- The expected issuer is `restaurant-api`.
- The gateway must use the same `JWT_SECRET` as the user service.
- Gateway authentication is an edge check only; the user service still performs its own authentication and authorization.

When changing gateway authentication, update tests for public endpoints, missing token, invalid token, and valid token.

## User Service

User is the business service for registration, authentication, and user management.

- Module: `user`.
- Main class: `br.com.fiap.user.UserApplication`.
- Port: `8080`.
- Spring role: Spring MVC, Spring Security, JPA, OpenAPI-generated interfaces, Eureka client.
- Docker container: `user-service`.
- Database: PostgreSQL container `user-postgres`.

Primary API groups:

- `POST /api/v1/auth/login`: public login endpoint; returns JWT.
- `POST /api/v1/users`: public user creation endpoint.
- `GET /api/v1/users`: admin-only user listing.
- `GET /api/v1/users/search`: admin-only user search by name.
- `GET /api/v1/users/{userId}`: admin or owner of the resource.
- `PUT /api/v1/users/{userId}`: admin or owner of the resource.
- `PATCH /api/v1/users/{userId}/password`: owner of the resource.

Package conventions:

- `application/domain`: entities, value objects, enums, pagination, and domain exceptions.
- `application/ports/inbound`: input ports used by controllers and filters.
- `application/ports/outbound`: output ports used by use cases.
- `application/usecases`: business use cases and orchestration.
- `infrastructure/inbound`: REST controllers, security filters, models, exception handlers, and mappers.
- `infrastructure/outbound`: persistence adapters, JPA repositories/entities, password encoding, and JWT adapter.
- `infrastructure/config`: Spring configuration and initial data setup.

Security behavior:

- `SecurityConfig` defines public routes and requires authentication for all other routes.
- `SecurityFilter` recovers the bearer token, validates it, loads the user, and populates the Spring Security context.
- `AuthenticateUserUseCase` handles login, token validation, and user lookup by token.
- `JwtTokenAdapter` signs and verifies JWT with issuer `restaurant-api`.
- Method authorization uses `@PreAuthorize` in `UserController`.

OpenAPI behavior:

- `user/src/main/resources/api/openapi.yml` is the source of truth for generated API interfaces and DTOs.
- If endpoint paths, request/response shapes, or operation names change, update the OpenAPI contract and the affected controllers/tests together.

## Order Service

Order owns order creation and order status.

- Module: `order`.
- Main class: `br.com.fiap.order.OrderApplication`.
- Port: `8081`.
- Spring role: Spring MVC, JPA, Kafka producer/consumer, Eureka client.
- Docker container: `order-service`.
- Database: PostgreSQL container `order-postgres`.
- Kafka producer topic: `payment-requests` through transactional outbox.
- Kafka consumer topic: `payment-results`.

Primary API groups:

- `POST /api/v1/orders`: creates an order as `PENDING_PAYMENT` and persists a pending payment request outbox event.
- `GET /api/v1/orders`: lists orders.
- `GET /api/v1/orders/{orderId}`: returns one order.

Package conventions mirror clean architecture:

- `application/domain/order`: order entity, item, and status.
- `application/ports/inbound/order`: use-case interfaces and request/response records.
- `application/ports/outbound`: repository and messaging ports.
- `application/usecases/order`: business orchestration.
- `infrastructure/inbound/rest`: REST controller and DTOs.
- `infrastructure/inbound/kafka`: payment result consumer.
- `infrastructure/outbound/kafka`: Kafka sender for payment request events.
- `infrastructure/outbound/persistence`: JPA entities, repositories, adapters, and outbox persistence.
- `infrastructure/outbound/persistence/outbox`: outbox entity, repository, messaging-port adapter, and scheduled publisher.

## Payment Service

Payment processes order payment requests asynchronously.

- Module: `payment`.
- Main class: `br.com.fiap.payment.PaymentApplication`.
- Port: `8082`.
- Spring role: Spring MVC runtime, JPA, Kafka producer/consumer, Eureka client, Resilience4j.
- Docker container: `payment-service`.
- Database: PostgreSQL container `payment-postgres`.
- Kafka consumer topic: `payment-requests`.
- Kafka producer topic: `payment-results` through transactional outbox.

Processing behavior:

- Consumes `payment-requests` from Kafka.
- Creates or reuses a payment for the order.
- Calls a simulated payment gateway through an outbound port.
- Uses Resilience4j `@Retry` and `@CircuitBreaker` around the simulated gateway.
- Persists a pending `payment-results` outbox event with `approved=true` or `approved=false`.
- A scheduled outbox publisher sends pending rows to Kafka and marks successful rows as `PUBLISHED`.
- Uses Kafka retry topic support and a `.DLT` dead-letter topic fallback for failed message processing.

Package conventions mirror clean architecture:

- `application/domain/payment`: payment entity, status, and authorization result.
- `application/ports/inbound/payment`: processing use-case interface and records.
- `application/ports/outbound`: gateway, repository, and messaging ports.
- `application/usecases/payment`: payment orchestration.
- `infrastructure/inbound/kafka`: payment request consumer and DLT fallback.
- `infrastructure/outbound/gateway`: simulated payment provider adapter.
- `infrastructure/outbound/kafka`: Kafka sender for payment result events.
- `infrastructure/outbound/persistence`: JPA entities, repositories, adapters, and outbox persistence.
- `infrastructure/outbound/persistence/outbox`: outbox entity, repository, messaging-port adapter, and scheduled publisher.
