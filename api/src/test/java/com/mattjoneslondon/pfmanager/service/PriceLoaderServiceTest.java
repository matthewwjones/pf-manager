package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.client.EodhdClient;
import com.mattjoneslondon.pfmanager.client.EodhdPriceRecord;
import com.mattjoneslondon.pfmanager.dao.EomPriceRepository;
import com.mattjoneslondon.pfmanager.dao.ExchangeRateRepository;
import com.mattjoneslondon.pfmanager.dao.instrument.InstrumentRepository;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceLoaderServiceTest {

    private static final LocalDate EOM_DATE = LocalDate.of(2026, 1, 31);
    private static final String SGLN_TICKER = "SGLN.L";
    private static final String GBP = "GBP";
    private static final double TOLERANCE = 0.0001;

    @Mock private EodhdClient eodhdClient;
    @Mock private InstrumentRepository instrumentRepository;
    @Mock private EomPriceRepository eomPriceRepository;
    @Mock private ExchangeRateRepository exchangeRateRepository;

    private PriceLoaderService priceLoaderService;

    @BeforeEach
    void setUp() {
        priceLoaderService = new PriceLoaderService(eodhdClient,
                                                    instrumentRepository,
                                                    eomPriceRepository,
                                                    exchangeRateRepository);
    }

    @Test
    void fetchesAndStoresPricesForEachInstrument() {
        Instrument sgln = new Instrument(SGLN_TICKER, "iShares Gold", GBP, 10.0);
        EodhdPriceRecord priceRecord = new EodhdPriceRecord("2026-01-31", 28.0, 29.0, 27.0, 28.5, 28.5, 100000L);

        when(instrumentRepository.findAll()).thenReturn(List.of(sgln));
        when(eodhdClient.fetchMonthlyPrices(eq(SGLN_TICKER), any(), any())).thenReturn(List.of(priceRecord));
        when(eodhdClient.fetchMonthlyExchangeRates(any(), any(), any())).thenReturn(List.of());

        priceLoaderService.loadPricesForDate(EOM_DATE);

        ArgumentCaptor<EomPrice> priceCaptor = ArgumentCaptor.forClass(EomPrice.class);
        verify(eomPriceRepository).upsert(priceCaptor.capture());
        EomPrice stored = priceCaptor.getValue();

        assertAll(
                () -> assertThat(stored.ticker(), is(SGLN_TICKER)),
                () -> assertThat(stored.closingPrice(), closeTo(28.5, TOLERANCE)),
                () -> assertThat(stored.priceDate(), is(LocalDate.of(2026, 1, 31)))
        );
    }

    @Test
    void storesInvertedGbpUsdRateAsUsdToGbp() {
        double gbpToUsd = 1.27;
        EodhdPriceRecord forexRecord = new EodhdPriceRecord("2026-01-31", 1.27, 1.28, 1.26, 1.27, gbpToUsd, 0L);

        when(instrumentRepository.findAll()).thenReturn(List.of());
        when(eodhdClient.fetchMonthlyExchangeRates(any(), any(), any())).thenReturn(List.of(forexRecord));

        priceLoaderService.loadPricesForDate(EOM_DATE);

        ArgumentCaptor<ExchangeRate> rateCaptor = ArgumentCaptor.forClass(ExchangeRate.class);
        verify(exchangeRateRepository).upsert(rateCaptor.capture());
        ExchangeRate stored = rateCaptor.getValue();

        assertAll(
                () -> assertThat(stored.fromCurrency(), is("USD")),
                () -> assertThat(stored.toCurrency(), is("GBP")),
                () -> assertThat(stored.rate(), closeTo(1.0 / gbpToUsd, TOLERANCE))
        );
    }
}
