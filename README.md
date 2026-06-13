## 📋 Sobre o Projeto

O **microservices** é uma plataforma back-end construída com arquitetura de microsserviços para gerenciamento de usuários. O projeto aplica boas práticas de engenharia de software como **Arquitetura Hexagonal (Ports & Adapters)**, **Clean Architecture** e **Domain-Driven Design (DDD)**.

Todo o tráfego de entrada passa pelo **API Gateway**, que roteia as requisições para os serviços registrados no **Eureka Discovery Server** — garantindo descoberta de serviços dinâmica e balanceamento de carga.

---

## Arquitetura

```
Cliente (HTTP)
     │
     ▼
┌─────────────────┐
│  API Gateway    │  :9090
│  (Spring Cloud) │
└────────┬────────┘
         │ lb://user (via Eureka)
         ▼
┌─────────────────┐     ┌─────────────────┐
│  User Service   │────▶│   PostgreSQL    │
│  :8080          │     │   :5432         │
└─────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐
│  Eureka Server  │  :8761
│  (Service Reg.) │
└─────────────────┘
```

---

## 🧩 Microsserviços

| Serviço | Descrição | Porta |
|---|---|---|
| `eureka-service` | Service registry — todos os serviços se registram aqui | `8761` |
| `gateway-service` | Ponto de entrada único, roteia requisições via load balancer | `9090` |
| `user-service` | CRUD de usuários, autenticação JWT, controle de acesso | `8080` |
| `order-service` | Criação e consulta de pedidos, publicação de solicitações de pagamento | `8081` |
| `payment-service` | Processamento assíncrono de pagamentos via Kafka | `8082` |
| `kafka` | Broker de eventos para pagamentos de pedidos | `29092` |

---

## 🛠️ Tecnologias

- **Java 25**
- **Spring Boot 4.x**
- **Spring Cloud Gateway** (WebFlux)
- **Spring Cloud Netflix Eureka**
- **Spring Security** + **JWT** (Auth0)
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL 17**
- **Apache Kafka**
- **Resilience4j** (Circuit Breaker + Retry)
- **MapStruct**
- **OpenAPI / Swagger** (geração de código via openapi-generator)
- **Docker** + **Docker Compose**

---

## 📐 Padrões e Boas Práticas

- **Arquitetura Hexagonal** — separação entre domínio, portas de entrada/saída e infraestrutura
- **Use Cases** explícitos por operação de negócio
- **Domain-Driven Design** — entidades ricas, Value Objects (`UserId`, `AddressId`)
- **OpenAPI First** — contrato da API definido em `openapi.yml`, código gerado automaticamente
- **Healthchecks** em todos os containers com dependências ordenadas no Compose
- **Multi-stage Dockerfile** — build separado do runtime para imagens enxutas

---

## 🚀 Como rodar o projeto

### Pré-requisitos

- [Docker](https://www.docker.com/) e Docker Compose instalados

### Subir todos os serviços

```bash
git clone https://github.com/seu-usuario/ecom-microservices.git
cd ecom-microservices
docker compose up --build
```

A ordem de inicialização é gerenciada automaticamente pelos healthchecks:

1. `user-postgres` → aguarda o banco estar pronto
2. `eureka-service` → aguarda o Eureka estar saudável
3. `user-service` e `gateway-service` → sobem após o Eureka

Acompanhe o status dos containers:

```bash
docker compose ps
```

---

## 📡 Endpoints

Todas as requisições devem ser feitas pela porta do **Gateway** (`9090`).

O Gateway valida localmente a assinatura, o emissor e a expiração do JWT usando a mesma `JWT_SECRET` do `user-service`.
Os endpoints públicos são login, criação de usuário, preflight `OPTIONS` e health checks.

### Autenticação

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | ❌ Público | Autentica e retorna JWT |

### Usuários

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/users` | ❌ Público | Cria novo usuário |
| `GET` | `/api/v1/users` | ✅ Bearer | Lista usuários paginado |
| `GET` | `/api/v1/users/{id}` | ✅ Bearer | Busca usuário por ID |
| `GET` | `/api/v1/users/search?name=` | ✅ Bearer | Busca usuários por nome |
| `PUT` | `/api/v1/users/{id}` | ✅ Bearer | Atualiza usuário |
| `PATCH` | `/api/v1/users/{id}/password` | ✅ Bearer | Atualiza senha |

### Pedidos

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/orders` | ✅ Bearer | Cria pedido e envia solicitação de pagamento para Kafka |
| `GET` | `/api/v1/orders` | ✅ Bearer | Lista pedidos |
| `GET` | `/api/v1/orders/{id}` | ✅ Bearer | Busca pedido por ID |

O fluxo de pagamento é assíncrono: o `order-service` publica em `payment-requests`, o `payment-service` processa com retry/circuit breaker/fallback e publica o resultado em `payment-results`.

---

## 🧪 Exemplos de uso

### Criar usuário
```bash
curl -X POST http://localhost:9090/api/v1/users \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"João Silva\",\"email\":\"joao@email.com\",\"login\":\"joao\",\"password\":\"123456\",\"userType\":\"CLIENT\",\"roles\":[\"USER\"],\"address\":{\"street\":\"Rua das Flores\",\"number\":\"42\",\"city\":\"São Paulo\",\"state\":\"SP\",\"zipCode\":\"01310-100\"}}"
```

### Login
```bash
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"joao\",\"password\":\"123456\"}"
```

### Listar usuários (com token)
```bash
curl http://localhost:9090/api/v1/users \
  -H "Authorization: Bearer <seu_token_jwt>"
```

---

## 🔍 Monitoramento

| URL | Descrição |
|---|---|
| `http://localhost:8761` | Painel do Eureka — serviços registrados |
| `http://localhost:9090/actuator/health` | Health do Gateway |
| `http://localhost:8080/actuator/health` | Health do User Service |

---

## 📁 Estrutura do Projeto

```
ecom-microservices/
├── docker-compose.yml
├── eureka/                        # Eureka Discovery Server
│   ├── Dockerfile
│   └── src/
├── gateway/                       # API Gateway (Spring Cloud)
│   ├── Dockerfile
│   └── src/
└── user/                          # User Service
    ├── Dockerfile
    └── src/
        └── main/java/br/com/fiap/user/
            ├── application/
            │   ├── domain/        # Entidades e regras de negócio
            │   ├── ports/         # Interfaces inbound/outbound
            │   └── usecases/      # Casos de uso da aplicação
            └── infrastructure/
                ├── inbound/       # Controllers, Security Filters
                └── outbound/      # JPA Repositories, JWT Adapter
```

---

## 📄 Licença

Este projeto está sob a licença MIT.
