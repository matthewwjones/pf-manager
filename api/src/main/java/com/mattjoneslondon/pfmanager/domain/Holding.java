package com.mattjoneslondon.pfmanager.domain;

import java.time.LocalDate;

public record Holding(
        long id,
        String ticker,
        double shares,
        LocalDate effectiveDate
) {
}