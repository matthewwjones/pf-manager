package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.repository.EomPriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;

@Service
public class MovingAverageService {

    private static final int MOVING_AVERAGE_MONTHS = 10;

    private final EomPriceRepository eomPriceRepository;

    public MovingAverageService(EomPriceRepository eomPriceRepository) {
        this.eomPriceRepository = eomPriceRepository;
    }

    public OptionalDouble calculateTenMonthMovingAverage(String ticker) {
        List<EomPrice> recentPrices = eomPriceRepository.findMostRecentForTicker(ticker, MOVING_AVERAGE_MONTHS);
        return averageOf(recentPrices);
    }

    public boolean hasEnoughHistoryForMovingAverage(String ticker) {
        List<EomPrice> prices = eomPriceRepository.findMostRecentForTicker(ticker, MOVING_AVERAGE_MONTHS);
        return prices.size() == MOVING_AVERAGE_MONTHS;
    }

    private OptionalDouble averageOf(List<EomPrice> prices) {
        if (prices.isEmpty()) {
            return OptionalDouble.empty();
        }
        double sum = prices.stream().mapToDouble(EomPrice::closingPrice).sum();
        return OptionalDouble.of(sum / prices.size());
    }
}
