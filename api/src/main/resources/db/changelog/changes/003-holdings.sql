--liquibase formatted sql
--changeset mattjoneslondon:003-holdings
CREATE TABLE holdings (
    id             INT         AUTO_INCREMENT PRIMARY KEY,
    ticker         VARCHAR(20) NOT NULL,
    shares         DOUBLE      NOT NULL,
    effective_date DATE        NOT NULL,
    UNIQUE (ticker, effective_date),
    FOREIGN KEY (ticker) REFERENCES instruments(ticker)
);
