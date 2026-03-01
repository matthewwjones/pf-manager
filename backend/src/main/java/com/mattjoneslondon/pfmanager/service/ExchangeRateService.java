package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import com.mattjoneslondon.pfmanager.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class ExchangeRateService {

    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public double getUsdToGbpRate(LocalDate date) {
        ExchangeRate rate = exchangeRateRepository.findLatestOnOrBefore(USD, GBP, date)
                .orElseThrow(() -> new NoSuchElementException(
                        "No USD/GBP exchange rate found on or before " + date
                ));
        return rate.rate();
    }
}
