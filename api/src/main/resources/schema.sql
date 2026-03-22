CREATE TABLE IF NOT EXISTS instruments (
    ticker            TEXT PRIMARY KEY,
    name              TEXT NOT NULL,
    currency          TEXT NOT NULL,
    target_weight_pct REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS eom_prices (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    ticker        TEXT    NOT NULL REFERENCES instruments(ticker),
    price_date    DATE    NOT NULL,
    closing_price REAL    NOT NULL,
    currency      TEXT    NOT NULL,
    UNIQUE(ticker, price_date)
);

CREATE TABLE IF NOT EXISTS holdings (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    ticker         TEXT    NOT NULL REFERENCES instruments(ticker),
    shares         REAL    NOT NULL,
    effective_date DATE    NOT NULL,
    UNIQUE(ticker, effective_date)
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    rate_date     DATE NOT NULL,
    from_currency TEXT NOT NULL,
    to_currency   TEXT NOT NULL,
    rate          REAL NOT NULL,
    UNIQUE(rate_date, from_currency, to_currency)
);