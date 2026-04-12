package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.dao.EomPriceRepository;
import com.mattjoneslondon.pfmanager.domain.EomPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class MovingAverageService {

    private static final int MOVING_AVERAGE_MONTHS = 10;
    private final EomPriceRepository eomPriceRepository;

    public OptionalDouble calculateTenMonthMovingAverage(String ticker) {
        final List<EomPrice> recentPrices = eomPriceRepository.findMostRecentForTicker(ticker, MOVING_AVERAGE_MONTHS);
        return averageOf(recentPrices);
    }

    public boolean hasEnoughHistoryForMovingAverage(String ticker) {
        final List<EomPrice> prices = eomPriceRepository.findMostRecentForTicker(ticker, MOVING_AVERAGE_MONTHS);
        return prices.size() == MOVING_AVERAGE_MONTHS;
    }

    OptionalDouble averageOf(List<EomPrice> prices) {
        if (prices.isEmpty()) {
            return OptionalDouble.empty();
        }
        final double sum = prices.stream().mapToDouble(EomPrice::closingPrice).sum();
        return OptionalDouble.of(sum / prices.size());
    }
}