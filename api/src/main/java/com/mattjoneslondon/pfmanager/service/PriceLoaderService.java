package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.client.EodhdClient;
import com.mattjoneslondon.pfmanager.client.EodhdPriceRecord;
import com.mattjoneslondon.pfmanager.dao.EomPriceRepository;
import com.mattjoneslondon.pfmanager.dao.ExchangeRateRepository;
import com.mattjoneslondon.pfmanager.dao.instrument.InstrumentRepository;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
public class PriceLoaderService {
    private static final String FOREX_TICKER = "GBPUSD.FOREX";
    private static final String GBP = "GBP";
    private static final String USD = "USD";
    private static final int HISTORY_FETCH_MONTHS = 12;
    private final EodhdClient eodhdClient;
    private final InstrumentRepository instrumentRepository;
    private final EomPriceRepository eomPriceRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public PriceLoaderService(EodhdClient eodhdClient,
                              InstrumentRepository instrumentRepository,
                              EomPriceRepository eomPriceRepository,
                              ExchangeRateRepository exchangeRateRepository) {
        this.eodhdClient = eodhdClient;
        this.instrumentRepository = instrumentRepository;
        this.eomPriceRepository = eomPriceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void loadPricesForDate(LocalDate eomDate) {
        final LocalDate fetchFrom = eomDate.minusMonths(HISTORY_FETCH_MONTHS);
        final List<Instrument> instruments = instrumentRepository.findAll();
        log.info("Loading prices for {} instrument(s) from {} to {}", instruments.size(), fetchFrom, eomDate);
        instruments.forEach(instrument -> loadPricesForInstrument(instrument, fetchFrom, eomDate));
        loadExchangeRates(fetchFrom, eomDate);
        log.info("Price load complete for end-of-month date {}", eomDate);
    }

    @Scheduled(cron = "0 0 18 L * ?")
    public void loadLatestPrices() {
        final LocalDate lastDayOfCurrentMonth = YearMonth.now().atEndOfMonth();
        log.info("Scheduled price load triggered for {}", lastDayOfCurrentMonth);
        loadPricesForDate(lastDayOfCurrentMonth);
    }

    void loadPricesForInstrument(Instrument instrument, LocalDate from, LocalDate to) {
        log.debug("Fetching prices for {} ({}) from {} to {}", instrument.ticker(), instrument.currency(), from, to);
        final List<EodhdPriceRecord> records = eodhdClient.fetchMonthlyPrices(instrument.ticker(), from, to);
        log.debug("Received {} record(s) for {}", records.size(), instrument.ticker());
        records.forEach(record -> storePrice(record, instrument.ticker(), instrument.currency()));
    }

    void storePrice(EodhdPriceRecord record, String ticker, String currency) {
        final EomPrice price = new EomPrice(0, ticker, LocalDate.parse(record.date()), record.adjustedClose(), currency);
        eomPriceRepository.upsert(price);
    }

    void loadExchangeRates(LocalDate from, LocalDate to) {
        log.debug("Fetching exchange rates for {} from {} to {}", FOREX_TICKER, from, to);
        final List<EodhdPriceRecord> records = eodhdClient.fetchMonthlyExchangeRates(FOREX_TICKER, from, to);
        log.debug("Received {} exchange rate record(s)", records.size());
        records.forEach(this::storeGbpToUsdRate);
    }

    void storeGbpToUsdRate(EodhdPriceRecord record) {
        final double gbpToUsd = record.adjustedClose();
        final double usdToGbp = 1.0 / gbpToUsd;
        final ExchangeRate rate = new ExchangeRate(0, LocalDate.parse(record.date()), USD, GBP, usdToGbp);
        exchangeRateRepository.upsert(rate);
    }
}
