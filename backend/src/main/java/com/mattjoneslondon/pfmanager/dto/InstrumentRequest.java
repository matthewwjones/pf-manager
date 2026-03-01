package com.mattjoneslondon.pfmanager.dto;

public record InstrumentRequest(String ticker, String name, String currency, double targetWeightPct) {
}
