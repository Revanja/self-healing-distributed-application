# Self-Healing Distributed Application

A reliability-engineering application that injects controlled failures into a distributed system, detects them, executes recovery, and independently verifies application-level recovery.

## Architecture

```text
React Dashboard
      |
      v
Order Service ----REST----> Payment Service
      ^                         ^
      |                         |
      +---- Reliability Controller ----+
```

### Core principle

**A remediation action succeeding is not the same thing as the application recovering.**

The controller therefore verifies:
1. dependency health,
2. a real payment operation,
3. an end-to-end order operation.

## Services

- Order Service — `8081`
- Payment Service — `8082`
- Reliability Controller — `8083`
- React Dashboard — `5173`

## Local prerequisites

- Java 17+
- Maven 3.6.3+
- Node.js 20+

Spring Boot 4.1.1 requires Java 17+.

## Run

Terminal 1:
```bash
cd services/payment-service
mvn spring-boot:run
```

Terminal 2:
```bash
cd services/order-service
mvn spring-boot:run
```

Terminal 3:
```bash
cd services/reliability-controller
mvn spring-boot:run
```

Terminal 4:
```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Experiment

1. Open the dashboard.
2. Confirm all services are healthy.
3. Click **Inject Payment Failure**.
4. Click **Execute Remediation**.
5. Click **Verify Recovery**.
6. Observe the incident lifecycle.

## API

```text
GET  /payments/health
GET  /payments
POST /payments/process
POST /payments/fault
DELETE /payments/fault

GET  /orders/health
GET  /orders
POST /orders

GET  /api/reliability/status
GET  /api/incidents
POST /api/failures/payment
POST /api/recovery/payment
POST /api/recovery/payment/verify
```

## Engineering roadmap

- [x] Two independent Spring Boot services
- [x] REST dependency
- [x] Controlled failure injection
- [x] Recovery action
- [x] Independent recovery verification
- [x] React control center
- [ ] persistent incidents
- [ ] richer failure modes
- [ ] Docker Compose verification
- [ ] container restart remediation
- [ ] integration tests
- [ ] CI/CD
- [ ] cloud deployment
- [ ] controlled experiments and measured results
