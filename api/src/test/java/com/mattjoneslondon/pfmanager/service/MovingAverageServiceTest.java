package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.dao.EomPriceRepository;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovingAverageServiceTest {

    private static final String TICKER = "SGLN.L";
    private static final String GBP = "GBP";
    private static final double TOLERANCE = 0.001;

    @Mock
    private EomPriceRepository eomPriceRepository;

    private MovingAverageService movingAverageService;

    @BeforeEach
    void setUp() {
        movingAverageService = new MovingAverageService(eomPriceRepository);
    }

    @Test
    void calculatesArithmeticMeanOfTenMonths() {
        List<EomPrice> tenMonthsOfPrices = buildPricesWithValues(
                10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0
        );
        when(eomPriceRepository.findMostRecentForTicker(TICKER, 10)).thenReturn(tenMonthsOfPrices);

        OptionalDouble result = movingAverageService.calculateTenMonthMovingAverage(TICKER);

        assertAll(
                () -> assertThat(result.isPresent(), is(true)),
                () -> assertThat(result.getAsDouble(), closeTo(55.0, TOLERANCE))
        );
    }

    @Test
    void returnsEmptyWhenNoPricesExist() {
        when(eomPriceRepository.findMostRecentForTicker(TICKER, 10)).thenReturn(List.of());

        OptionalDouble result = movingAverageService.calculateTenMonthMovingAverage(TICKER);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    void averagesAvailableDataWhenFewerThanTenMonths() {
        List<EomPrice> fiveMonthsOfPrices = buildPricesWithValues(10.0, 20.0, 30.0, 40.0, 50.0);
        when(eomPriceRepository.findMostRecentForTicker(TICKER, 10)).thenReturn(fiveMonthsOfPrices);

        OptionalDouble result = movingAverageService.calculateTenMonthMovingAverage(TICKER);

        assertAll(
                () -> assertThat(result.isPresent(), is(true)),
                () -> assertThat(result.getAsDouble(), closeTo(30.0, TOLERANCE))
        );
    }

    @Test
    void returnsTrueWhenTenMonthsOfPricesExist() {
        List<EomPrice> tenMonthsOfPrices = buildPricesWithValues(
                10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0
        );
        when(eomPriceRepository.findMostRecentForTicker(TICKER, 10)).thenReturn(tenMonthsOfPrices);

        assertThat(movingAverageService.hasEnoughHistoryForMovingAverage(TICKER), is(true));
    }

    @Test
    void returnsFalseWhenFewerThanTenMonthsExist() {
        List<EomPrice> fiveMonthsOfPrices = buildPricesWithValues(10.0, 20.0, 30.0, 40.0, 50.0);
        when(eomPriceRepository.findMostRecentForTicker(TICKER, 10)).thenReturn(fiveMonthsOfPrices);

        assertThat(movingAverageService.hasEnoughHistoryForMovingAverage(TICKER), is(false));
    }

    private List<EomPrice> buildPricesWithValues(double... values) {
        var prices = new ArrayList<EomPrice>();
        for (int i = 0; i < values.length; i++) {
            prices.add(new EomPrice(i + 1L, TICKER, LocalDate.now().minusMonths(i), values[i], GBP));
        }
        return prices;
    }
}