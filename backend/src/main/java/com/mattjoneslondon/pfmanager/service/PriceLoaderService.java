package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.client.EodhdClient;
import com.mattjoneslondon.pfmanager.client.EodhdPriceRecord;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.domain.ExchangeRate;
import com.mattjoneslondon.pfmanager.domain.Instrument;
import com.mattjoneslondon.pfmanager.repository.EomPriceRepository;
import com.mattjoneslondon.pfmanager.repository.ExchangeRateRepository;
import com.mattjoneslondon.pfmanager.repository.InstrumentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

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

    public PriceLoaderService(
            EodhdClient eodhdClient,
            InstrumentRepository instrumentRepository,
            EomPriceRepository eomPriceRepository,
            ExchangeRateRepository exchangeRateRepository
    ) {
        this.eodhdClient = eodhdClient;
        this.instrumentRepository = instrumentRepository;
        this.eomPriceRepository = eomPriceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void loadPricesForDate(LocalDate eomDate) {
        LocalDate fetchFrom = eomDate.minusMonths(HISTORY_FETCH_MONTHS);
        List<Instrument> instruments = instrumentRepository.findAll();
        instruments.forEach(instrument -> loadPricesForInstrument(instrument, fetchFrom, eomDate));
        loadExchangeRates(fetchFrom, eomDate);
    }

    @Scheduled(cron = "0 0 18 L * ?")
    public void loadLatestPrices() {
        LocalDate lastDayOfCurrentMonth = YearMonth.now().atEndOfMonth();
        loadPricesForDate(lastDayOfCurrentMonth);
    }

    private void loadPricesForInstrument(Instrument instrument, LocalDate from, LocalDate to) {
        List<EodhdPriceRecord> records = eodhdClient.fetchMonthlyPrices(instrument.ticker(), from, to);
        records.forEach(record -> storePrice(record, instrument.ticker(), instrument.currency()));
    }

    private void storePrice(EodhdPriceRecord record, String ticker, String currency) {
        EomPrice price = new EomPrice(0, ticker, LocalDate.parse(record.date()), record.adjustedClose(), currency);
        eomPriceRepository.upsert(price);
    }

    private void loadExchangeRates(LocalDate from, LocalDate to) {
        List<EodhdPriceRecord> records = eodhdClient.fetchMonthlyExchangeRates(FOREX_TICKER, from, to);
        records.forEach(this::storeGbpToUsdRate);
    }

    private void storeGbpToUsdRate(EodhdPriceRecord record) {
        double gbpToUsd = record.adjustedClose();
        double usdToGbp = 1.0 / gbpToUsd;
        ExchangeRate rate = new ExchangeRate(0, LocalDate.parse(record.date()), USD, GBP, usdToGbp);
        exchangeRateRepository.upsert(rate);
    }
}
