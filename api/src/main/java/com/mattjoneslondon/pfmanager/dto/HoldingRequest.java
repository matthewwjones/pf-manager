package com.mattjoneslondon.pfmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Request body for creating a holding")
public record HoldingRequest(
        @Schema(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker,
        @Schema(description = "Number of shares held", example = "12.5") double shares,
        @Schema(description = "Date from which this holding is effective (ISO-8601)", example = "2025-03-31") LocalDate effectiveDate
) {
}