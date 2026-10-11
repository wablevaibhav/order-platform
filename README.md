# Order Platform

Event-driven microservices on Java 21 and Spring Boot 3, connected through Apache Kafka.

```
POST /api/orders ──> order-service ──(orders.created)──> Kafka ──> notification-service ──> GET /api/notifications
                       PostgreSQL: orders                            PostgreSQL: notifications
```

| Service | Port | Responsibility |
|---|---|---|
| `order-service` | 8081 | Validates and stores orders (Spring Data JPA, PostgreSQL), publishes an `OrderCreatedEvent` to the `orders.created` topic |
| `notification-service` | 8082 | Consumes `orders.created` and stores a notification per order; exposes them over REST |

**Stack:** Spring Boot, Spring Web, Spring Data JPA (Hibernate), Spring for Apache Kafka, PostgreSQL,
Bean Validation, JUnit 5, Mockito, MockMvc, Docker / Docker Compose (Kafka in KRaft mode).

## Design notes

- **Message key = order id**, so all events for one order go to the same partition and stay ordered. Topic has 3 partitions.
- **Idempotent consumer:** Kafka delivers at least once, so `notification-service` checks for an existing notification
  (and `order_id` has a unique constraint) before creating one.
- **Poison messages:** malformed payloads are logged and skipped instead of blocking the partition.
- **Tolerant reader:** the consumer ignores unknown JSON fields, so the producer can add fields safely.
- **Known limitation:** the event is published right after the database save, not atomically with it. If the process
  crashes between the two, an order exists without an event. The standard fix is the transactional outbox pattern
  (see next steps).

## Run everything

```bash
docker compose up --build
```

```bash
curl -s -X POST localhost:8081/api/orders -H 'Content-Type: application/json' \
  -d '{"customerEmail":"buyer@example.com","product":"Keyboard","quantity":2}'

curl -s "localhost:8082/api/notifications?email=buyer@example.com"
```

## Run tests

Unit and slice tests use mocks, so no Kafka or database is required:

```bash
mvn test
```

## Next steps

Transactional outbox for atomic publish, dead-letter topic for failed messages, Testcontainers integration tests
with real Kafka and PostgreSQL, a shared events module, Flyway migrations.
