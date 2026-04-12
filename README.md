# Portfolio Manager

A portfolio management application built with Java 21 and Spring Boot 4. Tracks equity holdings, fetches end-of-month
prices from the [EODHD](https://eodhd.com) API, and generates buy/sell signals using a 10-month moving average strategy.

## Features

- Track instruments (equities) with target portfolio weights
- Record share holdings over time
- Fetch and store end-of-month closing prices from EODHD
- USD/GBP currency conversion via stored exchange rates
- Portfolio analytics: current value, weight drift, and 10-month MA signals (Buy / Sell / Neutral)
- REST API with Swagger UI
- MySQL persistence

## Tech Stack

- Java 21
- Spring Boot 4.0.0
- Spring JDBC (MySQL via `com.mysql:mysql-connector-j`)
- SpringDoc OpenAPI 3 (Swagger UI)
- JUnit 5 + Mockito + OkHttp MockWebServer
- Gradle 9 with version catalog

## Prerequisites

- Java 21+
- MySQL 8+
- An [EODHD API key](https://eodhd.com) (free tier available)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mattjoneslondon/pf-manager.git
cd pf-manager
```

### 2. Configure environment variables

| Variable               | Description              | Default                                    |
|------------------------|--------------------------|--------------------------------------------|
| `EODHD_API_KEY`        | Your EODHD API key       | _(empty)_                                  |
| `PORTFOLIO_DB_URL`     | JDBC URL for MySQL       | `jdbc:mysql://localhost:3306/pfman`        |
| `PORTFOLIO_DB_USERNAME`| Database username        | `root`                                     |
| `PORTFOLIO_DB_PASSWORD`| Database password        | _(empty)_                                  |

```bash
export EODHD_API_KEY=your_api_key_here
export PORTFOLIO_DB_URL=jdbc:mysql://localhost:3306/pfman
export PORTFOLIO_DB_USERNAME=your_db_user
export PORTFOLIO_DB_PASSWORD=your_db_password
```

### 3. Run the application

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

## API Reference

Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### Portfolio

| Method | Path             | Description                                                              |
|--------|------------------|--------------------------------------------------------------------------|
| `GET`  | `/api/portfolio` | Portfolio summary with analytics. Optional `?date=YYYY-MM-DD` parameter. |

### Instruments

| Method   | Path                        | Description                 |
|----------|-----------------------------|-----------------------------|
| `GET`    | `/api/instruments`          | List all instruments        |
| `POST`   | `/api/instruments`          | Create an instrument        |
| `GET`    | `/api/instruments/{ticker}` | Get an instrument by ticker |
| `PUT`    | `/api/instruments/{ticker}` | Update an instrument        |
| `DELETE` | `/api/instruments/{ticker}` | Delete an instrument        |

### Holdings

| Method   | Path                     | Description                    |
|----------|--------------------------|--------------------------------|
| `GET`    | `/api/holdings`          | List all holdings              |
| `GET`    | `/api/holdings/{ticker}` | Holdings for a specific ticker |
| `POST`   | `/api/holdings`          | Record a holding               |
| `DELETE` | `/api/holdings/{id}`     | Delete a holding               |

### Prices

| Method | Path                   | Description                       |
|--------|------------------------|-----------------------------------|
| `GET`  | `/api/prices`          | All stored prices                 |
| `GET`  | `/api/prices/{ticker}` | Prices for a specific ticker      |
| `POST` | `/api/prices/load`     | Fetch and store prices from EODHD |

## Development

### Build

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

Tests use an in-memory H2 database (MySQL mode) and a mock EODHD server — no external dependencies required.

### Test coverage

JaCoCo coverage reports are generated at `api/build/reports/jacoco/`.

## Database Schema

The schema is initialised automatically on startup from `api/src/main/resources/schema.sql`.

| Table            | Description                                                    |
|------------------|----------------------------------------------------------------|
| `instruments`    | Instrument definitions (ticker, name, currency, target weight) |
| `eom_prices`     | End-of-month closing prices                                    |
| `holdings`       | Share quantities with effective dates                          |
| `exchange_rates` | Currency conversion rates (e.g. USD/GBP)                       |

## Portfolio Analytics

The `GET /api/portfolio` endpoint returns per-instrument analytics including:

- Current market value (converted to GBP)
- Actual vs target portfolio weight
- 10-month simple moving average
- Signal: **Buy** (price > 10M MA), **Sell** (price < 10M MA), or **Neutral** (insufficient data)

Prices are automatically fetched on the last calendar day of each month at 18:00.
