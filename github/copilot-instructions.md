# Phi-Trading Exchange – GitHub Copilot Project Instructions

This repository contains my individual SoftUni “Spring Advanced” project.

## System Overview

The solution consists of two Spring Boot 3.4 / Java 17 applications:

1) Main application – `phi-trading-exchange-main`
    - Tech:
        - Build: Maven
        - DB: MySQL (separate DB)
        - Spring Web, Spring MVC + Thymeleaf
        - Spring Data JPA
        - Spring Security
        - Validation (spring-boot-starter-validation)
        - Spring Cache
        - Spring Scheduling
        - OpenFeign (REST client to microservice)
        - MySQL driver
        - Lombok (optional but preferred)
    - Base package: `com.phitrading.exchange`

2) REST microservice – `market-pricing-service`
    - Tech:
        - Build: Maven
        - DB: MySQL (separate DB)
        - Spring Web, Spring Data JPA
        - Spring Cache, Spring Scheduling
        - Validation, MySQL driver, Lombok
    - Base package: `com.phitrading.pricing`

## Architecture and Coding Rules

- Use **UUID** as primary key type for all entities.
- Use **Spring Data JPA** repositories.
- Follow **layered architecture**:
    - domain.entity
    - domain.repository
    - domain.service / domain.service.impl
    - web.controller
    - web.dto
    - web.security
    - web.mapper
    - common.exception, common.util
- Controllers must be **thin**: no business logic in controllers, only in services.
- Keep code clean:
    - follow Java naming conventions
    - no unused imports, no dead code, no TODOs left
- Add logging for each valid domain functionality.
- For each application, plan for:
    - at least 1 unit test, 1 integration test, 1 API test
    - target ~80% line coverage.

## Main Application – Domain

Simulated stock exchange where a user:

- registers, logs in and has a **cash wallet**
- owns a **portfolio of stocks** (real-world tickers like AAPL, TSLA, NVDA)
- can **buy and sell** stocks using prices from the pricing microservice
- can view portfolio, open orders, trade history and total portfolio value

Core entities (example fields):

**UserAccount**
- id: UUID
- username (unique)
- email (unique)
- passwordHash (BCrypt)
- roles: Set<Role> or enum/list
- cashBalance: BigDecimal
- createdAt, updatedAt  
  Passwords are always hashed; integrate with Spring Security user details.

**PortfolioPosition**
- id: UUID
- user: UserAccount (ManyToOne)
- symbol: String
- quantity: BigDecimal or long
- averagePrice: BigDecimal
- createdAt, updatedAt

**Order**
- id: UUID
- user: UserAccount (ManyToOne)
- symbol: String
- quantity: BigDecimal or long
- side: enum { BUY, SELL }
- status: enum { PENDING, EXECUTED, CANCELED }
- executionPrice: BigDecimal (null when PENDING)
- createdAt, executedAt  
  Execution price is retrieved from the pricing microservice when the order is executed.

Other technical entities like Role, Audit etc. are allowed.

### Main App – Key Functionalities

Design valid domain functionalities (POST/PUT/DELETE + state change + visible result), including:

- User registration with validation and initial cashBalance (e.g. 10 000.00)
- Deposit cash into wallet (authenticated user, amount form)
- Buy stock (market order):
    - get current price via Feign from microservice
    - check sufficient cash
    - create EXECUTED Order
    - update cashBalance and PortfolioPosition (quantity + averagePrice)
- Sell stock:
    - check sufficient quantity
    - get price from microservice
    - create EXECUTED Order
    - update cashBalance and PortfolioPosition (delete when quantity 0)
- Optional: cancel PENDING orders
- Admin: add tradable symbol
    - optionally persist local symbol
    - call pricing microservice `POST /api/instruments`

User-only functions (registration, profile edit) do **not** count as domain functionalities for the exam, so make sure to have at least 6 involving Order / PortfolioPosition / InstrumentPrice.

## REST Microservice – Domain and Endpoints

`market-pricing-service` is a simple price engine.

Entity **InstrumentPrice**:
- id: UUID
- symbol: String (unique)
- name: String
- lastPrice: BigDecimal
- previousClose: BigDecimal
- updatedAt: LocalDateTime

Functionalities:

- `POST /api/instruments` – create/update instrument (symbol, name, initialPrice)
- `PUT /api/instruments/{symbol}/price` – update price after trade
- `GET /api/instruments/{symbol}/price` – return current lastPrice  
  Main app calls these via Feign.

## Security

- Use Spring Security:
    - USER: can view own profile, deposit, buy/sell, see portfolio and orders
    - ADMIN: can manage tradable symbols, possibly view global stats
- Endpoint access:
    - Public: home, login, registration, optional public price overview
    - Authenticated: portfolio, orders, deposit, trade
    - Admin-only: symbol management, admin dashboards
- Enable CSRF for Thymeleaf.
- Passwords always with BCrypt.

## Scheduling and Caching

Microservice:
- `@Scheduled(cron = "0 */1 * * * *")` – every minute simulate random ±% price moves, persist and log.
- Cache `GET /api/instruments/{symbol}/price` with `@Cacheable`, invalidate on updates.

Main app:
- At least one scheduled job (e.g. recalculate cached portfolio valuations or top gainers).

## Validation and Error Handling

Both apps:

- Validate DTOs using annotations (@NotNull, @Size, @Positive, etc.).
- Validate business rules in services (insufficient funds, insufficient quantity, symbol not tradable).
- Have:
    - handler for a built-in exception (e.g. MethodArgumentNotValidException)
    - handler for a custom exception (e.g. InsufficientFundsException, PositionNotFoundException)
- Never expose white-label error pages; return meaningful error views or JSON.

## Frontend – Thymeleaf

Main app uses Thymeleaf with a layout template and modern HTML/CSS (main.css).  
Minimum pages:

- `/` – home (public)
- `/auth/login`
- `/auth/register`
- `/dashboard`
- `/portfolio`
- `/orders`
- `/trade/buy`
- `/trade/sell`
- `/admin/symbols`
- `/profile`

Focus on clear forms and tables; styling is done via `main.css`.

## How Copilot should behave

When generating code:

- Respect the architecture and rules above.
- Prefer:
    - clean, production-style Java
    - constructor injection
    - well-named services, repositories, DTOs and controllers
- Keep templates consistent with the existing layout and CSS.
- Do not add unnecessary complexity; prefer simple, correct solutions.
