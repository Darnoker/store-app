# Storeapp — Repository Context

## Purpose

Storeapp is an e-commerce application built incrementally around microservices. The long-term plan includes event-driven communication through Kafka, containerization, Kubernetes, AWS, and Spark/Scala analytics. The roadmap is detailed in `eventshop_project_plan.md`.

## Current architecture

- The root project is the `storeapp` Maven aggregator (`packaging: pom`).
- Stack: Java 26, Spring Boot 4.1.1, Maven Wrapper.
- Current module: `modules/order-service`.
- `order-service` exposes `POST /orders` and `GET /orders/{orderId}`, persists orders in PostgreSQL with JPA, and versions the schema with Flyway.
- Docker Compose runs PostgreSQL at `localhost:5432`; services use isolated `order-service` and `product-service` databases, with matching `-test` databases for integration tests.

## Spring Boot service conventions

Every Spring Boot microservice should preserve the following design decisions:

1. The API contract is the source of truth: OpenAPI lives in `src/main/resources/openapi/`.
2. OpenAPI Generator creates controller interfaces and models in `target/generated-sources`; do not edit those files manually.
3. A controller implements the generated interface and delegates to a domain service.
4. Domain logic remains in a functional package, with JPA in its `persistence` subpackage.
5. Mapping across layer boundaries is explicit and handled by mappers.
6. Flyway migrations are immutable after use; schema changes receive the next `V<n>__description.sql` file.
7. Obtain time through `Clock` in application code so the logic can be tested deterministically.

## Package conventions

```text
com.github.darnoker.<servicename>
├── api/                 HTTP implementations and API mapping
├── config/              technical beans, e.g. Clock
└── <domain>/
    ├── model/           domain models
    └── persistence/     JPA entities, repositories, and persistence mapping
```

Use meaningful package names for the service and its functional domains.

## Key files

| Purpose | File |
| --- | --- |
| Module aggregation and shared versions | `pom.xml` |
| A service module | `modules/<name>-service/pom.xml` |
| REST contract | `modules/<name>-service/src/main/resources/openapi/<name>-api.yaml` |
| Application configuration | `modules/<name>-service/src/main/resources/application.yaml` |
| Database migrations | `modules/<name>-service/src/main/resources/db/migration/` |
| Local infrastructure | `docker-compose.yml` |

## Verification

```powershell
.\mvnw test
# or one service only
.\mvnw -pl modules/<name>-service test
```

Before integration tests, start PostgreSQL:

```powershell
docker compose up -d postgres
```
