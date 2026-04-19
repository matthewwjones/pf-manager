--liquibase formatted sql
--changeset mattjoneslondon:001-instruments
CREATE TABLE instruments (
    ticker            VARCHAR(20)  PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    currency          VARCHAR(10)  NOT NULL,
    target_weight_pct DOUBLE       NOT NULL
);
