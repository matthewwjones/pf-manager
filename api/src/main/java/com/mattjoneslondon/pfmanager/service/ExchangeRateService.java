package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.dao.ExchangeRateRepository;
import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private final ExchangeRateRepository exchangeRateRepository;

    public double getUsdToGbpRate(LocalDate date) {
        final ExchangeRate rate = exchangeRateRepository.findLatestOnOrBefore(USD, GBP, date)
                .orElseThrow(() -> new NoSuchElementException(
                        "No USD/GBP exchange rate found on or before " + date
                ));
        return rate.rate();
    }
}