package com.mattjoneslondon.pfmanager.domain.holding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A record of shares held in an instrument from a given effective date")
public record Holding(
        @Schema(description = "Unique holding ID", example = "1") long id,
        @Schema(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker,
        @Schema(description = "Number of shares held", example = "12.5") double shares,
        @Schema(description = "Date from which this holding is effective (ISO-8601)", example = "2025-03-31") LocalDate effectiveDate
) {
}
