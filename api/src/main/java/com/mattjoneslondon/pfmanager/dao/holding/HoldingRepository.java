package com.mattjoneslondon.pfmanager.dao.holding;

import com.mattjoneslondon.pfmanager.domain.holding.Holding;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HoldingRepository {

    private final JdbcTemplate jdbcTemplate;

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
                ON DUPLICATE KEY UPDATE
                    shares = VALUES(shares)
                """,
                holding.ticker(),
                holding.shares(),
                holding.effectiveDate().toString()
        );
    }

    public void deleteById(long id) {
        jdbcTemplate.update("DELETE FROM holdings WHERE id = ?", id);
    }

    RowMapper<Holding> rowMapper() {
        return (rs, rowNum) -> new Holding(
                rs.getLong("id"),
                rs.getString("ticker"),
                rs.getDouble("shares"),
                LocalDate.parse(rs.getString("effective_date"))
        );
    }
}