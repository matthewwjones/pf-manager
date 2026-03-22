package com.mattjoneslondon.pfmanager.repository;

import com.mattjoneslondon.pfmanager.domain.Holding;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class HoldingRepository {

    private final JdbcTemplate jdbcTemplate;

    public HoldingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Holding> findAll() {
        return jdbcTemplate.query(
                "SELECT id, ticker, shares, effective_date FROM holdings ORDER BY ticker, effective_date DESC",
                rowMapper()
        );
    }

    public List<Holding> findByTicker(String ticker) {
        return jdbcTemplate.query(
                "SELECT id, ticker, shares, effective_date FROM holdings WHERE ticker = ? ORDER BY effective_date DESC",
                rowMapper(),
                ticker
        );
    }

    public Optional<Holding> findLatestForTickerOnOrBefore(String ticker, LocalDate date) {
        return jdbcTemplate.query(
                """
                SELECT id, ticker, shares, effective_date
                FROM holdings
                WHERE ticker = ? AND effective_date <= ?
                ORDER BY effective_date DESC
                LIMIT 1
                """,
                rowMapper(),
                ticker,
                date.toString()
        ).stream().findFirst();
    }

    public void upsert(Holding holding) {
        jdbcTemplate.update(
                """
                INSERT INTO holdings(ticker, shares, effective_date)
                VALUES (?, ?, ?)
                ON CONFLICT(ticker, effective_date) DO UPDATE SET
                    shares = excluded.shares
                """,
                holding.ticker(),
                holding.shares(),
                holding.effectiveDate().toString()
        );
    }

    public void deleteById(long id) {
        jdbcTemplate.update("DELETE FROM holdings WHERE id = ?", id);
    }

    private RowMapper<Holding> rowMapper() {
        return (rs, rowNum) -> new Holding(
                rs.getLong("id"),
                rs.getString("ticker"),
                rs.getDouble("shares"),
                LocalDate.parse(rs.getString("effective_date"))
        );
    }
}