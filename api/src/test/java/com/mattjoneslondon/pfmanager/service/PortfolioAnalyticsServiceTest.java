package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.domain.Holding;
import com.mattjoneslondon.pfmanager.domain.Instrument;
import com.mattjoneslondon.pfmanager.dto.InstrumentAnalyticsDto;
import com.mattjoneslondon.pfmanager.dto.PortfolioSummaryDto;
import com.mattjoneslondon.pfmanager.repository.EomPriceRepository;
import com.mattjoneslondon.pfmanager.repository.HoldingRepository;
import com.mattjoneslondon.pfmanager.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioAnalyticsServiceTest {

    private static final double TOLERANCE = 0.001;
    private static final LocalDate EOM_DATE = LocalDate.of(2026, 1, 31);
    private static final String SGLN_TICKER = "SGLN.L";
    private static final String GBP = "GBP";

    @Mock private InstrumentRepository instrumentRepository;
    @Mock private EomPriceRepository eomPriceRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private MovingAverageService movingAverageService;
    @Mock private ExchangeRateService exchangeRateService;

    private PortfolioAnalyticsService portfolioAnalyticsService;

    @BeforeEach
    void setUp() {
        portfolioAnalyticsService = new PortfolioAnalyticsService(instrumentRepository,
                                                                  eomPriceRepository,
                                                                  holdingRepository,
                                                                  movingAverageService,
                                                                  exchangeRateService);
    }

    @Test
    void signalIsBuyWhenPriceAboveMovingAverage() {
        Instrument sgln = new Instrument(SGLN_TICKER, "iShares Gold", GBP, 10.0);
        EomPrice latestPrice = new EomPrice(1L, SGLN_TICKER, EOM_DATE, 30.0, GBP);
        Holding holding = new Holding(1L, SGLN_TICKER, 500.0, EOM_DATE);

        when(instrumentRepository.findAll()).thenReturn(List.of(sgln));
        when(eomPriceRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(latestPrice));
        when(movingAverageService.calculateTenMonthMovingAverage(SGLN_TICKER))
                .thenReturn(OptionalDouble.of(25.0));
        when(holdingRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(holding));

        PortfolioSummaryDto summary = portfolioAnalyticsService.buildPortfolioSummary(EOM_DATE);

        assertAll(
                () -> assertThat(summary.instruments(), hasSize(1)),
                () -> assertThat(summary.instruments().get(0).signal(), is("BUY"))
        );
    }

    @Test
    void signalIsSellWhenPriceBelowMovingAverage() {
        Instrument sgln = new Instrument(SGLN_TICKER, "iShares Gold", GBP, 10.0);
        EomPrice latestPrice = new EomPrice(1L, SGLN_TICKER, EOM_DATE, 20.0, GBP);
        Holding holding = new Holding(1L, SGLN_TICKER, 500.0, EOM_DATE);

        when(instrumentRepository.findAll()).thenReturn(List.of(sgln));
        when(eomPriceRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(latestPrice));
        when(movingAverageService.calculateTenMonthMovingAverage(SGLN_TICKER))
                .thenReturn(OptionalDouble.of(25.0));
        when(holdingRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(holding));

        PortfolioSummaryDto summary = portfolioAnalyticsService.buildPortfolioSummary(EOM_DATE);

        assertThat(summary.instruments().get(0).signal(), is("SELL"));
    }

    @Test
    void calculatesValueAndPctDiffFromMovingAverage() {
        Instrument sgln = new Instrument(SGLN_TICKER, "iShares Gold", GBP, 10.0);
        EomPrice latestPrice = new EomPrice(1L, SGLN_TICKER, EOM_DATE, 30.0, GBP);
        Holding holding = new Holding(1L, SGLN_TICKER, 1000.0, EOM_DATE);

        when(instrumentRepository.findAll()).thenReturn(List.of(sgln));
        when(eomPriceRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(latestPrice));
        when(movingAverageService.calculateTenMonthMovingAverage(SGLN_TICKER))
                .thenReturn(OptionalDouble.of(25.0));
        when(holdingRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.of(holding));

        PortfolioSummaryDto summary = portfolioAnalyticsService.buildPortfolioSummary(EOM_DATE);
        InstrumentAnalyticsDto analytics = summary.instruments().get(0);

        assertAll(
                () -> assertThat(analytics.valueGbp(), closeTo(30000.0, TOLERANCE)),
                () -> assertThat(analytics.pctDiffFromMa(), closeTo(20.0, TOLERANCE)),
                () -> assertThat(summary.totalValueGbp(), closeTo(30000.0, TOLERANCE)),
                () -> assertThat(analytics.currentWeightPct(), closeTo(100.0, TOLERANCE))
        );
    }

    @Test
    void convertsUsdPriceToGbpWhenInstrumentIsUsd() {
        Instrument usdInstrument = new Instrument("AIGC.L", "AI ETF", "USD", 10.0);
        EomPrice latestPrice = new EomPrice(1L, "AIGC.L", EOM_DATE, 100.0, "USD");
        Holding holding = new Holding(1L, "AIGC.L", 100.0, EOM_DATE);
        double usdToGbp = 0.80;

        when(instrumentRepository.findAll()).thenReturn(List.of(usdInstrument));
        when(eomPriceRepository.findLatestForTickerOnOrBefore("AIGC.L", EOM_DATE))
                .thenReturn(Optional.of(latestPrice));
        when(movingAverageService.calculateTenMonthMovingAverage("AIGC.L"))
                .thenReturn(OptionalDouble.of(90.0));
        when(holdingRepository.findLatestForTickerOnOrBefore("AIGC.L", EOM_DATE))
                .thenReturn(Optional.of(holding));
        when(exchangeRateService.getUsdToGbpRate(EOM_DATE)).thenReturn(usdToGbp);

        PortfolioSummaryDto summary = portfolioAnalyticsService.buildPortfolioSummary(EOM_DATE);
        InstrumentAnalyticsDto analytics = summary.instruments().get(0);

        assertThat(analytics.valueGbp(), closeTo(8000.0, TOLERANCE));
    }

    @Test
    void signalIsNeutralWhenNoPriceExists() {
        Instrument sgln = new Instrument(SGLN_TICKER, "iShares Gold", GBP, 10.0);

        when(instrumentRepository.findAll()).thenReturn(List.of(sgln));
        when(eomPriceRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.empty());
        when(movingAverageService.calculateTenMonthMovingAverage(SGLN_TICKER))
                .thenReturn(OptionalDouble.empty());
        when(holdingRepository.findLatestForTickerOnOrBefore(SGLN_TICKER, EOM_DATE))
                .thenReturn(Optional.empty());

        PortfolioSummaryDto summary = portfolioAnalyticsService.buildPortfolioSummary(EOM_DATE);

        assertThat(summary.instruments().get(0).signal(), is("NEUTRAL"));
    }
}
