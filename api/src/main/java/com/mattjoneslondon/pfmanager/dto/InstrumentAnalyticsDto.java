package com.mattjoneslondon.pfmanager.dto;

public record InstrumentAnalyticsDto(
        String ticker,
        String name,
        String currency,
        double latestPrice,
        double movingAverage10m,
        String signal,
        double pctDiffFromMa,
        double shares,
        double valueGbp,
        double currentWeightPct,
        double targetWeightPct
) {
    public InstrumentAnalyticsDto withCurrentWeightPct(double weight) {
        return new InstrumentAnalyticsDto(
                ticker, name, currency, latestPrice, movingAverage10m,
                signal, pctDiffFromMa, shares, valueGbp, weight, targetWeightPct
        );
    }
}
