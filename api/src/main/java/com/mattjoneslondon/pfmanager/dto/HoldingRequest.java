package com.mattjoneslondon.pfmanager.dto;

import java.time.LocalDate;

public record HoldingRequest(String ticker, double shares, LocalDate effectiveDate) {
}
