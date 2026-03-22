package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.dto.PortfolioSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

@Tag(name = "Portfolio", description = "Portfolio analytics and valuation summary")
public interface PortfolioController {

    @Operation(
            summary = "Get portfolio summary",
            description = "Returns an analytics summary of the entire portfolio for the given date. " +
                    "Defaults to the end of the current month when no date is supplied."
    )
    @ApiResponse(responseCode = "200", description = "Portfolio summary",
            content = @Content(schema = @Schema(implementation = PortfolioSummaryDto.class)))
    PortfolioSummaryDto getPortfolioSummary(
            @Parameter(description = "Valuation date (ISO-8601). Defaults to end of current month.", example = "2025-03-31")
            LocalDate date);
}