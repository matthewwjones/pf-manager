--liquibase formatted sql
--changeset mattjoneslondon:002-eom-prices
CREATE TABLE eom_prices (
    id            INT         AUTO_INCREMENT PRIMARY KEY,
    ticker        VARCHAR(20) NOT NULL,
    price_date    DATE        NOT NULL,
    closing_price DOUBLE      NOT NULL,
    currency      VARCHAR(10) NOT NULL,
    UNIQUE (ticker, price_date),
    FOREIGN KEY (ticker) REFERENCES instruments(ticker)
);
