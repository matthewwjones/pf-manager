package com.mattjoneslondon.pfmanager.domain;

import java.time.LocalDate;

public record ExchangeRate(
        long id,
        LocalDate rateDate,
        String fromCurrency,
        String toCurrency,
        double rate
) {
}