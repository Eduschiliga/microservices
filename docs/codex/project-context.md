# Project Context

## Architecture

This repository contains a small Spring-based microservices platform centered on user management and authentication.
The intended runtime path is:

```text
Client
  -> API Gateway (:9090)
  -> Eureka load-balanced route lb://user
  -> User Service (:8080)
  -> PostgreSQL (:5432)

Client
  -> API Gateway (:9090)
  -> Eureka load-balanced route lb://order
  -> Order Service (:8081)
  -> PostgreSQL (:5433)
  -> order_outbox table
  -> Kafka topic payment-requests
  -> Payment Service (:8082)
  -> PostgreSQL (:5434)
  -> payment_outbox table
  -> Kafka topic payment-results
  -> Order Service updates order status
```

Eureka runs separately on port `8761` and acts as the service registry.
The gateway uses Eureka discovery to route to the `user` service instead of hard-coding a host and port.

## Services and Containers

| Service | Container | Port | Responsibility |
|---|---|---:|---|
| Eureka | `eureka-service` | `8761` | Service registry for gateway and user service |
| Gateway | `gateway-service` | `9090` | Public entrypoint and route centralization |
| User | `user-service` | `8080` | User CRUD, login, JWT generation, and authorization |
| Order | `order-service` | `8081` | Order creation, order querying, and payment status updates |
| Payment | `payment-service` | `8082` | Asynchronous payment processing for orders |
| Kafka | `kafka` | `29092` host, `9092` internal | Payment request/result message broker |
| PostgreSQL | `user-postgres` | `5432` | Persistent storage for the user service |
| PostgreSQL | `order-postgres` | `5433` host, `5432` internal | Persistent storage for the order service |
| PostgreSQL | `payment-postgres` | `5434` host, `5432` internal | Persistent storage for the payment service |

## Runtime Dependencies

- `gateway-service` depends on `eureka-service`.
- `user-service` depends on `user-postgres` and `eureka-service`.
- `order-service` depends on `order-postgres`, `eureka-service`, and `kafka`.
- `payment-service` depends on `payment-postgres`, `eureka-service`, and `kafka`.
- Docker Compose healthchecks control startup order.
- `JWT_SECRET` must match between `gateway-service` and `user-service`.
- `JWT_EXPIRATION` is owned by `user-service`, because the user service generates tokens.
- Kafka topics used by the order/payment flow are `payment-requests` and `payment-results`.
- Kafka publications from `order` and `payment` use transactional outbox tables so database state and pending events are committed together before asynchronous delivery.

## Request Flow

1. Public clients call the gateway on `http://localhost:9090`.
2. The gateway checks whether the route is public.
3. For protected routes, the gateway validates JWT signature, issuer, and expiration locally.
4. Gateway routes matching `/api/v1/auth/**` and `/api/v1/users/**` to `lb://user`.
5. The user service validates authentication again and applies method-level authorization where required.
6. User data is persisted in PostgreSQL through Spring Data JPA repositories.
7. Gateway routes matching `/api/v1/orders/**` to `lb://order`.
8. The order service saves the order as `PENDING_PAYMENT` and stores a pending `PaymentRequested` event in `order_outbox` in the same transaction.
9. The order outbox publisher sends pending outbox rows to the `payment-requests` Kafka topic and marks successful rows as `PUBLISHED`.
10. The payment service consumes payment requests, applies retry/circuit-breaker/fallback behavior, stores a pending `PaymentResult` event in `payment_outbox`, and commits payment state with the event.
11. The payment outbox publisher sends pending outbox rows to the `payment-results` Kafka topic and marks successful rows as `PUBLISHED`.
12. The order service consumes payment results and updates orders to `PAID` or `PAYMENT_FAILED`.

## Important Configuration Files

- Root orchestration: `docker-compose.yml`.
- Gateway routes and JWT secret: `gateway/src/main/resources/application.yml`.
- User datasource, JWT secret, expiration, and resilience config: `user/src/main/resources/application.yml`.
- Order datasource, Kafka, and Eureka settings: `order/src/main/resources/application.yml`.
- Payment datasource, Kafka retry, Resilience4j, and Eureka settings: `payment/src/main/resources/application.yml`.
- Eureka server settings: `eureka/src/main/resources/application.yaml`.
- User API contract: `user/src/main/resources/api/openapi.yml`.
