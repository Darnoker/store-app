# Agent Instructions

## Working rules

- Before making a change, inspect the structure and existing conventions. Do not overwrite or remove unrelated user changes.
- Keep changes small and cohesive, and verify them with the appropriate Maven test.
- Do not edit generated files or `target/` directories.
- See `CODEX.md` for the detailed architecture and current repository state.

## Standard for Spring Boot microservices

When creating or extending a Spring Boot service, follow these conventions unless a requirement explicitly justifies a deviation:

- create a Maven module at `modules/<name>-service` and add it to the root `pom.xml` `<modules>` section;
- use the base package `com.github.darnoker.<servicename>`;
- define the HTTP contract in `src/main/resources/openapi/<name>-api.yaml`, generating interfaces and models with `openapi-generator-maven-plugin`;
- implement the generated controller interface in a custom controller; controllers must not contain business logic;
- separate code into `api`, a functional domain package (for example, `order`), `model`, and `persistence` layers;
- use records for simple domain models and explicit mappers between API, domain, and JPA entities;
- keep configuration in `application.yaml`, Flyway migrations in `src/main/resources/db/migration`, and use PostgreSQL as the local database;
- base API tests on `@SpringBootTest`, `MockMvc`, and the `test` profile.

## Build and test

- Run the Maven Wrapper from the repository root: `./mvnw test` or a targeted module command such as `./mvnw -pl modules/<name>-service test`.
- Integration tests expect a local PostgreSQL instance provided by `docker-compose.yml`; the test database is named `eventshop-test`.
- After changing an OpenAPI contract, Maven compilation/tests must regenerate the generated sources.
