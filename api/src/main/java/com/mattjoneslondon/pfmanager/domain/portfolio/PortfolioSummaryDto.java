package com.mattjoneslondon.pfmanager.domain.portfolio;

import com.mattjoneslondon.pfmanager.domain.instrument.InstrumentAnalyticsDto;

import java.time.LocalDate;
import java.util.List;

public record PortfolioSummaryDto(
        LocalDate date,
        double totalValueGbp,
        List<InstrumentAnalyticsDto> instruments
) {
}
