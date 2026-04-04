package com.mattjoneslondon.pfmanager.controller.portfolio;

import com.mattjoneslondon.pfmanager.domain.instrument.InstrumentAnalyticsDto;
import com.mattjoneslondon.pfmanager.domain.portfolio.PortfolioSummaryDto;
import com.mattjoneslondon.pfmanager.service.portfolio.PortfolioAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioControllerImpl.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioAnalyticsService portfolioAnalyticsService;

    @Test
    void returnsPortfolioAnalyticsWithOkStatus() throws Exception {
        InstrumentAnalyticsDto sgln = new InstrumentAnalyticsDto(
                "SGLN.L", "iShares Gold", "GBP", 30.0, 25.0, "BUY", 20.0, 1000.0, 30000.0, 100.0, 10.0
        );
        PortfolioSummaryDto summary = new PortfolioSummaryDto(
                LocalDate.of(2026, 1, 31), 30000.0, List.of(sgln)
        );
        when(portfolioAnalyticsService.buildPortfolioSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalValueGbp", closeTo(30000.0, 0.001)))
                .andExpect(jsonPath("$.instruments", hasSize(1)))
                .andExpect(jsonPath("$.instruments[0].ticker", is("SGLN.L")))
                .andExpect(jsonPath("$.instruments[0].signal", is("BUY")))
                .andExpect(jsonPath("$.instruments[0].pctDiffFromMa", closeTo(20.0, 0.001)));
    }
}
