# Parking Lot System - Assignment

This is a minimal Spring Boot project skeleton for the Parking Lot System coding assignment.

## Features included
- Java + Spring Boot (web, data-jpa)
- H2 in-memory DB for quick local testing
- Basic domain entities: Vehicle, ParkingSlot, Ticket, Payment
- Repositories, Services, Controllers (simple and easy to explain)
- Transactional allocation and exit flows (comments in code)
- Pessimistic locking example for slot allocation
- Sample Postman collection included
- OAuth2 (Google) placeholders in application.yml (optional for the assignment)
- Unit test example skeleton

## Build & Run
Requirements: Java 17+, Maven

```bash
mvn clean package
mvn spring-boot:run
```

H2 console available at `http://localhost:8080/h2-console` (JDBC URL: jdbc:h2:mem:parkingdb)

## What to explain in the review
- Why `@Transactional` is used around entry and exit flows
- How PESSIMISTIC_WRITE prevents double allocation
- Payment atomicity strategy and alternatives (async callbacks)
- Extensible allocation & pricing strategies (interfaces)
