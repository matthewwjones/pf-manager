package com.mattjoneslondon.pfmanager.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "An end-of-month closing price for an instrument")
public record EomPrice(
        @Schema(description = "Unique price record ID", example = "1") long id,
        @Schema(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker,
        @Schema(description = "End-of-month price date (ISO-8601)", example = "2025-03-31") LocalDate priceDate,
        @Schema(description = "Closing price in the instrument's trading currency", example = "108.24") double closingPrice,
        @Schema(description = "Trading currency (ISO 4217)", example = "GBP") String currency
) {
}