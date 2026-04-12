package com.mattjoneslondon.pfmanager.dao.instrument;

import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InstrumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Instrument> findAll() {
        return jdbcTemplate.query(
                "SELECT ticker, name, currency, target_weight_pct FROM instruments ORDER BY ticker",
                rowMapper()
        );
    }

    public Optional<Instrument> findByTicker(String ticker) {
        return jdbcTemplate.query(
                "SELECT ticker, name, currency, target_weight_pct FROM instruments WHERE ticker = ?",
                rowMapper(),
                ticker
        ).stream().findFirst();
    }

    public void save(Instrument instrument) {
        jdbcTemplate.update(
                """
                INSERT INTO instruments(ticker, name, currency, target_weight_pct)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    currency = VALUES(currency),
                    target_weight_pct = VALUES(target_weight_pct)
                """,
                instrument.ticker(),
                instrument.name(),
                instrument.currency(),
                instrument.targetWeightPct()
        );
    }

    public void deleteByTicker(String ticker) {
        jdbcTemplate.update("DELETE FROM instruments WHERE ticker = ?", ticker);
    }

    RowMapper<Instrument> rowMapper() {
        return (rs, rowNum) -> new Instrument(
                rs.getString("ticker"),
                rs.getString("name"),
                rs.getString("currency"),
                rs.getDouble("target_weight_pct")
        );
    }
}