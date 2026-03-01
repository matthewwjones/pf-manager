package com.mattjoneslondon.pfmanager.dto;

import java.time.LocalDate;
import java.util.List;

public record PortfolioSummaryDto(
        LocalDate date,
        double totalValueGbp,
        List<InstrumentAnalyticsDto> instruments
) {
}
