package com.mattjoneslondon.pfmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating or updating an instrument")
public record InstrumentRequest(
        @Schema(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker,
        @Schema(description = "Human-readable instrument name", example = "Vanguard FTSE All-World UCITS ETF") String name,
        @Schema(description = "Trading currency (ISO 4217)", example = "GBP") String currency,
        @Schema(description = "Target portfolio weight as a percentage (0–100)", example = "40.0") double targetWeightPct
) {
}