# DroneFleet — Drone Management System

**Status: Under Development / Portfolio Project**

A Java 17 desktop application for managing a drone fleet and simulating package deliveries. The project is designed as a portfolio-quality demonstration of object-oriented design, layered architecture, SQLite persistence, concurrency, authentication/RBAC, design patterns, auditing, and automated testing.

## Architecture

`Swing UI → Services → Domain / Strategy / Events → Repositories → SQLite`

Cross-cutting concerns include authentication, RBAC, configuration, logging, audit history, and CSV export.

## Features

- Standard, Heavy, and Express drone types
- Fleet management with capacity and battery validation
- Best-fit drone assignment strategy
- Delivery lifecycle: Pending → Assigned → In Transit → Completed / Failed
- Concurrent delivery simulation
- SQLite persistence
- Authentication with PBKDF2 password hashing
- ADMIN / OPERATOR roles
- Maintenance and recharge management
- Delivery history and operational analytics
- Audit trail
- CSV export
- JUnit test suite
- Maven build
- Executable shaded JAR packaging

## Requirements

- JDK 17+
- Maven 3.9+

## Run in IntelliJ IDEA

1. Open the folder containing `pom.xml`.
2. Import it as a Maven project.
3. Select JDK 17 or newer.
4. Run `com.dronefleet.app.Application`.

## Run with Maven

```bash
mvn clean test
mvn package
java -jar target/drone-management-system-2.0.0-SNAPSHOT.jar
```

The packaged JAR includes runtime dependencies through the Maven Shade Plugin.

## Development Accounts

- `admin / admin123` — administrative access
- `operator / operator123` — operational access; fleet administration is read-only

Change or replace these credentials before production use.

## Database

SQLite is initialized automatically from `src/main/resources/schema.sql`. The default database location is configured in `src/main/resources/application.properties`.

## Portfolio Positioning

The project demonstrates OOP, layered architecture, repository/service separation, Strategy Pattern, event-driven delivery updates, multithreading, relational persistence, authentication, RBAC, validation, testing, logging, and maintainable Java application structure.

## UX / Reliability Pass

The current build intentionally keeps the architecture shallow while addressing common desktop-demo issues:
- Recharge is simulated asynchronously with a visible 5-second progress dialog instead of instantly jumping to 100%.
- A `CHARGING` drone cannot be assigned, removed, or recharged again.
- Full batteries are rejected as a no-op recharge.
- Fleet and delivery tables are read-only to prevent misleading unsaved edits.
- Drone removal requires confirmation and is blocked when the drone has delivery history.
- Package IDs remain unique, including failed deliveries, so historical records cannot be overwritten.
- Interrupted deliveries are marked failed on the next startup and affected drones are released.
- Interrupted recharges return to a safe non-charging state on restart.
- Empty/non-numeric GUI input receives clearer validation messages.
