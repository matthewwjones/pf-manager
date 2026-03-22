package com.mattjoneslondon.pfmanager.domain;

public record Instrument(
        String ticker,
        String name,
        String currency,
        double targetWeightPct
) {
}