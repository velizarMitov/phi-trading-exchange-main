# Phi-Trading Exchange – Main Application

## Overview
Phi-Trading Exchange (main app) is a multi-page Spring Boot 3 web application that simulates a retail-style trading front-end. It provides pages for a user dashboard, portfolio, orders and exports, buy/sell trade flows, deposit, market overview, and admin symbol management.

The app consumes live prices from a separate REST microservice named `market-pricing-service` over HTTP using a Feign client.

## Features
- User authentication (form login) with roles:
  - USER – default trader permissions
  - ADMIN – admin-only features (instruments management)

- Dashboard
  - Greeting for the logged-in user
  - Total account value and P/L snapshot
  - Quick links to trade, portfolio, orders, and deposit

- Portfolio page (`/portfolio`)
  - Table of positions: symbol, quantity, average price, current price, P/L, P/L%
  - Live prices retrieved from the pricing microservice

- Orders page (`/orders`)
  - Full order history with statuses (EXECUTED / CANCELED) and colored badges
  - Sorting by date (in view/service)
  - Export to PDF and CSV:
    - CSV: `GET /orders/export/csv`
    - PDF: `GET /orders/export/pdf`

- Trade flow (`/trade/...`)
  - Buy flow: selects instrument and quantity, validates available cash, updates portfolio + cash
  - Sell flow: loads current position, limits quantity to available, computes realized P/L, updates portfolio + cash
  - Endpoints:
    - Buy form: `GET /trade/buy`
    - Submit buy: `POST /trade/buy`
    - Sell form: `GET /trade/sell`
    - Submit sell: `POST /trade/sell`

- Deposit
  - Simple deposit form to increase user cash balance (simulated, no real payment)

- Market Overview (`/market`)
  - Live list of all instruments with prices from the pricing service
  - Clean layout with table and indicators (advancers/decliners)

- Admin functionality (`/admin/symbols`)
  - Admin panel for managing instruments (create, edit, delete via pricing service)
  - Optional view who holds positions in an instrument (if implemented)

## Architecture
- Spring Boot 3, Java 17
- Thymeleaf MPA (multi-page application)
- Feign client for calling `market-pricing-service` (HTTP)
- JPA/Hibernate with a relational database (see application.properties for MySQL configuration)
- Security: Spring Security with roles USER and ADMIN

## Running the application
### Prerequisites
- Java 17+
- Maven
- A running instance of `market-pricing-service` (configure its base URL in application properties)

### Build & run
```bash
mvn clean install
mvn spring-boot:run
```

### Default URLs
- http://localhost:8080/ – Home / Login entry point
- /login, /register
- /dashboard
- /portfolio
- /orders
- /orders/export/csv, /orders/export/pdf
- /trade/buy, /trade/sell
- /market
- /admin/symbols
- /profile

## Configuration
Main configuration is in `src/main/resources/application.properties`.

- Pricing service base URL (Feign client):
  - `pricing.service.url=http://localhost:8081`

- Database connection (MySQL example):
  - `spring.datasource.url=jdbc:mysql://localhost:3306/phi_trading_main?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
  - `spring.datasource.username=your_user`
  - `spring.datasource.password=your_password`
  - `spring.jpa.hibernate.ddl-auto=update` (dev convenience)

- Default admin user
  - If provisioned via data.sql or an initializer, you can change credentials or seed data there.

## 🔐 Default Users

The application includes predefined users for evaluation:

### **Admin Account**

```pgsql
username: admin
password: 123123
role: ADMIN
```

```yaml
Admin panel:
  http://localhost:8080/admin/symbols
```

```sql
```

These credentials are automatically loaded from `data.sql` (or initializer) when the project starts.

### **User Account**

```pgsql
username: user
password: user123
role: USER
```

## Testing
The project includes a mix of unit, API (Web/MockMvc), and integration tests (H2/MySQL per configuration).

- Unit tests for services (e.g., portfolio, orders, pricing integration)
- Web/MockMvc tests for controllers (e.g., order exports)
- Integration tests using application context and database

Run all tests:
```bash
mvn test
```

## Project structure
Key packages follow a layered architecture:

- `com.phitrading.exchange.domain` – entities, repositories, services
- `com.phitrading.exchange.web` – DTOs for views, supporting classes
- `phitrading.phitradingexchangemain.web.controller` – MVC controllers and endpoints
- `com.phitrading.exchange.config` – shared configuration (security, Feign, etc.)

Thymeleaf templates live under `src/main/resources/templates` and static assets under `src/main/resources/static`.

## Future improvements / bonus features
- Optional OAuth2 login (Google/GitHub/Facebook)
- More advanced charts or real-time updates via WebSockets
- Support for more asset classes and advanced risk metrics
