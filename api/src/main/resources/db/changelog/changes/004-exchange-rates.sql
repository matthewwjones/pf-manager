--liquibase formatted sql
--changeset mattjoneslondon:004-exchange-rates
CREATE TABLE exchange_rates (
    id            INT         AUTO_INCREMENT PRIMARY KEY,
    rate_date     DATE        NOT NULL,
    from_currency VARCHAR(10) NOT NULL,
    to_currency   VARCHAR(10) NOT NULL,
    rate          DOUBLE      NOT NULL,
    UNIQUE (rate_date, from_currency, to_currency)
);
