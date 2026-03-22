package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import com.mattjoneslondon.pfmanager.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private static final double TOLERANCE = 0.0001;
    private static final LocalDate JANUARY_2026 = LocalDate.of(2026, 1, 31);

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService(exchangeRateRepository);
    }

    @Test
    void givenRateExists_whenGettingUsdToGbpRate_thenReturnsRate() {
        ExchangeRate storedRate = new ExchangeRate(1L, JANUARY_2026, USD, GBP, 0.7874);
        when(exchangeRateRepository.findLatestOnOrBefore(USD, GBP, JANUARY_2026))
                .thenReturn(Optional.of(storedRate));

        double rate = exchangeRateService.getUsdToGbpRate(JANUARY_2026);

        assertThat(rate, closeTo(0.7874, TOLERANCE));
    }

    @Test
    void givenNoRateExists_whenGettingUsdToGbpRate_thenThrowsNoSuchElementException() {
        when(exchangeRateRepository.findLatestOnOrBefore(USD, GBP, JANUARY_2026))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> exchangeRateService.getUsdToGbpRate(JANUARY_2026));
    }
}
