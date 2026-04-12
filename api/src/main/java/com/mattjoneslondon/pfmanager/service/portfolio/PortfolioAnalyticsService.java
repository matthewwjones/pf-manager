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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
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

    public PortfolioSummaryDto buildPortfolioSummary(LocalDate eomDate) {
        final List<Instrument> instruments = instrumentRepository.findAll();
        final List<InstrumentAnalyticsDto> analytics = instruments.stream()
                .map(instrument -> buildInstrumentAnalytics(instrument, eomDate))
                .toList();

        final double totalValueGbp = analytics.stream().mapToDouble(InstrumentAnalyticsDto::valueGbp).sum();
        final List<InstrumentAnalyticsDto> analyticsWithWeights = analytics.stream()
                .map(dto -> dto.withCurrentWeightPct(weightOf(dto.valueGbp(), totalValueGbp)))
                .toList();

        return new PortfolioSummaryDto(eomDate, totalValueGbp, analyticsWithWeights);
    }

    InstrumentAnalyticsDto buildInstrumentAnalytics(Instrument instrument, LocalDate eomDate) {
        final EomPrice latestPrice = eomPriceRepository.findLatestForTickerOnOrBefore(instrument.ticker(), eomDate)
                .orElse(null);
        final OptionalDouble movingAverage = movingAverageService.calculateTenMonthMovingAverage(instrument.ticker());
        final double shares = sharesHeldOn(instrument.ticker(), eomDate);
        final double priceInGbp = toPriceInGbp(latestPrice, instrument.currency(), eomDate);
        final double valueGbp = priceInGbp * shares;
        final String signal = determineSignal(latestPrice, movingAverage);
        final double pctDiffFromMa = calculatePctDiff(latestPrice, movingAverage);

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

    double sharesHeldOn(String ticker, LocalDate date) {
        return holdingRepository.findLatestForTickerOnOrBefore(ticker, date)
                .map(Holding::shares)
                .orElse(0.0);
    }

    double toPriceInGbp(EomPrice price, String currency, LocalDate date) {
        if (price == null) {
            return 0.0;
        }
        if (USD.equals(currency)) {
            final double usdToGbp = exchangeRateService.getUsdToGbpRate(date);
            return price.closingPrice() * usdToGbp;
        }
        return price.closingPrice();
    }

    String determineSignal(EomPrice latestPrice, OptionalDouble movingAverage) {
        if (latestPrice == null || movingAverage.isEmpty()) {
            return NEUTRAL_SIGNAL;
        }
        return latestPrice.closingPrice() > movingAverage.getAsDouble() ? BUY_SIGNAL : SELL_SIGNAL;
    }

    double calculatePctDiff(EomPrice latestPrice, OptionalDouble movingAverage) {
        if (latestPrice == null || movingAverage.isEmpty() || movingAverage.getAsDouble() == 0.0) {
            return 0.0;
        }
        return ((latestPrice.closingPrice() - movingAverage.getAsDouble()) / movingAverage.getAsDouble()) * 100.0;
    }

    double weightOf(double value, double total) {
        if (total == 0.0) {
            return 0.0;
        }
        return (value / total) * 100.0;
    }
}