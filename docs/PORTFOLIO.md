# Portfolio Summary

## DroneFleet — Drone Delivery & Fleet Management System

**Status:** Under Development

DroneFleet is a Java-based desktop platform that simulates and manages drone delivery operations. It combines fleet management, intelligent drone assignment, concurrent delivery simulation, battery and maintenance management, persistent SQLite storage, authentication/RBAC, audit logging, analytics, and CSV reporting.

### Tech Stack

Java 17, Swing, SQLite, Maven, JUnit 5, JDBC, Multithreading, Strategy Pattern, Repository/Service architecture.

### Engineering Highlights

- Extensible object-oriented domain model for drones, packages, and deliveries.
- Layered separation between Swing presentation, services, repositories, and persistence.
- Strategy-based drone assignment to isolate selection algorithms from delivery orchestration.
- Event bus for delivery lifecycle notifications without coupling business logic to the UI.
- Concurrent delivery simulation using an executor service.
- SQLite persistence for users, drones, packages, deliveries, maintenance, and audit records.
- PBKDF2 password hashing and role-based authorization.
- JUnit tests for security and domain rules.
