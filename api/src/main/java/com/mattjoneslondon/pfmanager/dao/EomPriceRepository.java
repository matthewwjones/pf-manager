package com.mattjoneslondon.pfmanager.dao;

import com.mattjoneslondon.pfmanager.domain.EomPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EomPriceRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<EomPrice> findByTicker(String ticker) {
        return jdbcTemplate.query(
                "SELECT id, ticker, price_date, closing_price, currency FROM eom_prices WHERE ticker = ? ORDER BY price_date DESC",
                rowMapper(),
                ticker
        );
    }

    public List<EomPrice> findAll() {
        return jdbcTemplate.query(
                "SELECT id, ticker, price_date, closing_price, currency FROM eom_prices ORDER BY ticker, price_date DESC",
                rowMapper()
        );
    }

    public Optional<EomPrice> findLatestForTickerOnOrBefore(String ticker, LocalDate date) {
        return jdbcTemplate.query(
                """
                        SELECT id, ticker, price_date, closing_price, currency
                        FROM eom_prices
                        WHERE ticker = ? AND price_date <= ?
                        ORDER BY price_date DESC
                        LIMIT 1
                        """,
                rowMapper(),
                ticker,
                date.toString()
        ).stream().findFirst();
    }

    public List<EomPrice> findMostRecentForTicker(String ticker, int count) {
        return jdbcTemplate.query(
                """
                        SELECT id, ticker, price_date, closing_price, currency
                        FROM eom_prices
                        WHERE ticker = ?
                        ORDER BY price_date DESC
                        LIMIT ?
                        """,
                rowMapper(),
                ticker,
                count
        );
    }

    public void upsert(EomPrice price) {
        jdbcTemplate.update(
                """
                        INSERT INTO eom_prices(ticker, price_date, closing_price, currency)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            closing_price = VALUES(closing_price),
                            currency = VALUES(currency)
                        """,
                price.ticker(),
                price.priceDate().toString(),
                price.closingPrice(),
                price.currency()
        );
    }

    RowMapper<EomPrice> rowMapper() {
        return (rs, rowNum) -> new EomPrice(
                rs.getLong("id"),
                rs.getString("ticker"),
                LocalDate.parse(rs.getString("price_date")),
                rs.getDouble("closing_price"),
                rs.getString("currency")
        );
    }
}