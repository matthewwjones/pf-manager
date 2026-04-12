package com.mattjoneslondon.pfmanager.dao;

import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExchangeRateRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<ExchangeRate> findLatestOnOrBefore(String fromCurrency, String toCurrency, LocalDate date) {
        return jdbcTemplate.query(
                """
                SELECT id, rate_date, from_currency, to_currency, rate
                FROM exchange_rates
                WHERE from_currency = ? AND to_currency = ? AND rate_date <= ?
                ORDER BY rate_date DESC
                LIMIT 1
                """,
                rowMapper(),
                fromCurrency,
                toCurrency,
                date.toString()
        ).stream().findFirst();
    }

    public void upsert(ExchangeRate exchangeRate) {
        jdbcTemplate.update(
                """
                INSERT INTO exchange_rates(rate_date, from_currency, to_currency, rate)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    rate = VALUES(rate)
                """,
                exchangeRate.rateDate().toString(),
                exchangeRate.fromCurrency(),
                exchangeRate.toCurrency(),
                exchangeRate.rate()
        );
    }

    RowMapper<ExchangeRate> rowMapper() {
        return (rs, rowNum) -> new ExchangeRate(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("rate_date")),
                rs.getString("from_currency"),
                rs.getString("to_currency"),
                rs.getDouble("rate")
        );
    }
}