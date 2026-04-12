CREATE TABLE IF NOT EXISTS instruments (
    ticker            VARCHAR(20)    PRIMARY KEY,
    name              VARCHAR(255)   NOT NULL,
    currency          VARCHAR(10)    NOT NULL,
    target_weight_pct DOUBLE         NOT NULL
);

CREATE TABLE IF NOT EXISTS eom_prices (
    id            INT          AUTO_INCREMENT PRIMARY KEY,
    ticker        VARCHAR(20)  NOT NULL,
    price_date    DATE         NOT NULL,
    closing_price DOUBLE       NOT NULL,
    currency      VARCHAR(10)  NOT NULL,
    UNIQUE (ticker, price_date),
    FOREIGN KEY (ticker) REFERENCES instruments(ticker)
);

CREATE TABLE IF NOT EXISTS holdings (
    id             INT         AUTO_INCREMENT PRIMARY KEY,
    ticker         VARCHAR(20) NOT NULL,
    shares         DOUBLE      NOT NULL,
    effective_date DATE        NOT NULL,
    UNIQUE (ticker, effective_date),
    FOREIGN KEY (ticker) REFERENCES instruments(ticker)
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id            INT         AUTO_INCREMENT PRIMARY KEY,
    rate_date     DATE        NOT NULL,
    from_currency VARCHAR(10) NOT NULL,
    to_currency   VARCHAR(10) NOT NULL,
    rate          DOUBLE      NOT NULL,
    UNIQUE (rate_date, from_currency, to_currency)
);