package com.mattjoneslondon.pfmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Request body for triggering a price load")
public record LoadPricesRequest(
        @Schema(description = "End-of-month date for which to load prices (ISO-8601). Defaults to end of current month when omitted.", example = "2025-03-31")
        LocalDate date
) {
}