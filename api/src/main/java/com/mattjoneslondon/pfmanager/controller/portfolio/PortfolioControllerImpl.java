package com.mattjoneslondon.pfmanager.controller.portfolio;

import com.mattjoneslondon.pfmanager.domain.portfolio.PortfolioSummaryDto;
import com.mattjoneslondon.pfmanager.service.portfolio.PortfolioAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioControllerImpl implements PortfolioController {

    private final PortfolioAnalyticsService portfolioAnalyticsService;

    public PortfolioControllerImpl(PortfolioAnalyticsService portfolioAnalyticsService) {
        this.portfolioAnalyticsService = portfolioAnalyticsService;
    }

    @Override
    @GetMapping
    public PortfolioSummaryDto getPortfolioSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate eomDate = date != null ? date : YearMonth.now().atEndOfMonth();
        return portfolioAnalyticsService.buildPortfolioSummary(eomDate);
    }
}
