# Architecture & Design Decisions

## Layers
- **UI:** Swing views and user interaction only.
- **Service:** application use cases and transaction orchestration.
- **Domain:** Drone, PackageItem, Delivery and lifecycle enums.
- **Strategy:** assignment algorithm isolated behind `AssignmentStrategy`.
- **Repository:** JDBC persistence and mapping.
- **Infrastructure:** SQLite, configuration and logging.
- **Security:** authentication, PBKDF2 password verification and session roles.

## Key decisions
### Repository Pattern
Database access is kept outside domain/services. This makes the application easier to test and gives persistence a single responsibility.

### Strategy Pattern
`AssignmentStrategy` allows future policies such as Best Fit, Lowest Battery Risk, Fastest Delivery, or Cost Optimized without rewriting `DeliveryService`.

### Event system
`EventBus` publishes delivery lifecycle events. The dashboard subscribes without the delivery service knowing about Swing components.

### State-oriented delivery lifecycle
`PENDING → ASSIGNED → IN_TRANSIT → COMPLETED` with `FAILED` as a terminal failure path.

### Concurrency
Delivery simulations run on an `ExecutorService`; Swing updates are marshalled back onto the EDT.
