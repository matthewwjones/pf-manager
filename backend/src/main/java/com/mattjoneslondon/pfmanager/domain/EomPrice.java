package com.mattjoneslondon.pfmanager.domain;

import java.time.LocalDate;

public record EomPrice(
        long id,
        String ticker,
        LocalDate priceDate,
        double closingPrice,
        String currency
) {
}