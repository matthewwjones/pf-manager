package com.mattjoneslondon.pfmanager.service.portfolio;

import com.mattjoneslondon.pfmanager.dao.EomPriceRepository;
import com.mattjoneslondon.pfmanager.dao.holding.HoldingRepository;
import com.mattjoneslondon.pfmanager.dao.instrument.InstrumentRepository;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.domain.holding.Holding;
import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import com.mattjoneslondon.pfmanager.domain.instrument.InstrumentAnalyticsDto;
import com.mattjoneslondon.pfmanager.domain.portfolio.PortfolioSummaryDto;
import com.mattjoneslondon.pfmanager.service.ExchangeRateService;
import com.mattjoneslondon.pfmanager.service.MovingAverageService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class PortfolioAnalyticsService {

    private static final String USD = "USD";
    private static final String BUY_SIGNAL = "BUY";
    private static final String SELL_SIGNAL = "SELL";
    private static final String NEUTRAL_SIGNAL = "NEUTRAL";
    private final InstrumentRepository instrumentRepository;
    private final EomPriceRepository eomPriceRepository;
    private final HoldingRepository holdingRepository;
    private final MovingAverageService movingAverageService;
    private final ExchangeRateService exchangeRateService;

    public PortfolioAnalyticsService(InstrumentRepository instrumentRepository,
                                     EomPriceRepository eomPriceRepository,
                                     HoldingRepository holdingRepository,
                                     MovingAverageService movingAverageService,
                                     ExchangeRateService exchangeRateService) {
        this.instrumentRepository = instrumentRepository;
        this.eomPriceRepository = eomPriceRepository;
        this.holdingRepository = holdingRepository;
        this.movingAverageService = movingAverageService;
        this.exchangeRateService = exchangeRateService;
    }

    public PortfolioSummaryDto buildPortfolioSummary(LocalDate eomDate) {
        List<Instrument> instruments = instrumentRepository.findAll();
        List<InstrumentAnalyticsDto> analytics = instruments.stream()
                .map(instrument -> buildInstrumentAnalytics(instrument, eomDate))
                .toList();

        double totalValueGbp = analytics.stream().mapToDouble(InstrumentAnalyticsDto::valueGbp).sum();
        List<InstrumentAnalyticsDto> analyticsWithWeights = analytics.stream()
                .map(dto -> dto.withCurrentWeightPct(weightOf(dto.valueGbp(), totalValueGbp)))
                .toList();

        return new PortfolioSummaryDto(eomDate, totalValueGbp, analyticsWithWeights);
    }

    private InstrumentAnalyticsDto buildInstrumentAnalytics(Instrument instrument, LocalDate eomDate) {
        EomPrice latestPrice = eomPriceRepository.findLatestForTickerOnOrBefore(instrument.ticker(), eomDate)
                .orElse(null);
        OptionalDouble movingAverage = movingAverageService.calculateTenMonthMovingAverage(instrument.ticker());
        double shares = sharesHeldOn(instrument.ticker(), eomDate);
        double priceInGbp = toPriceInGbp(latestPrice, instrument.currency(), eomDate);
        double valueGbp = priceInGbp * shares;
        String signal = determineSignal(latestPrice, movingAverage);
        double pctDiffFromMa = calculatePctDiff(latestPrice, movingAverage);

        return new InstrumentAnalyticsDto(
                instrument.ticker(),
                instrument.name(),
                instrument.currency(),
                latestPrice != null ? latestPrice.closingPrice() : 0.0,
                movingAverage.orElse(0.0),
                signal,
                pctDiffFromMa,
                shares,
                valueGbp,
                0.0,
                instrument.targetWeightPct()
        );
    }

    private double sharesHeldOn(String ticker, LocalDate date) {
        return holdingRepository.findLatestForTickerOnOrBefore(ticker, date)
                .map(Holding::shares)
                .orElse(0.0);
    }

    private double toPriceInGbp(EomPrice price, String currency, LocalDate date) {
        if (price == null) {
            return 0.0;
        }
        if (USD.equals(currency)) {
            double usdToGbp = exchangeRateService.getUsdToGbpRate(date);
            return price.closingPrice() * usdToGbp;
        }
        return price.closingPrice();
    }

    private String determineSignal(EomPrice latestPrice, OptionalDouble movingAverage) {
        if (latestPrice == null || movingAverage.isEmpty()) {
            return NEUTRAL_SIGNAL;
        }
        return latestPrice.closingPrice() > movingAverage.getAsDouble() ? BUY_SIGNAL : SELL_SIGNAL;
    }

    private double calculatePctDiff(EomPrice latestPrice, OptionalDouble movingAverage) {
        if (latestPrice == null || movingAverage.isEmpty() || movingAverage.getAsDouble() == 0.0) {
            return 0.0;
        }
        return ((latestPrice.closingPrice() - movingAverage.getAsDouble()) / movingAverage.getAsDouble()) * 100.0;
    }

    private double weightOf(double value, double total) {
        if (total == 0.0) {
            return 0.0;
        }
        return (value / total) * 100.0;
    }
}
