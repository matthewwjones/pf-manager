package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.Instrument;
import com.mattjoneslondon.pfmanager.service.InstrumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstrumentControllerImpl.class)
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstrumentService instrumentService;

    @Test
    void givenInstrumentsExist_whenGettingAll_thenReturnsListWithOkStatus() throws Exception {
        List<Instrument> instruments = List.of(
                new Instrument("SGLN.L", "iShares Gold", "GBP", 10.0),
                new Instrument("ISF.L", "iShares FTSE 100", "GBP", 15.0)
        );
        when(instrumentService.getAllInstruments()).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ticker", is("SGLN.L")))
                .andExpect(jsonPath("$[1].ticker", is("ISF.L")));
    }

    @Test
    void givenValidRequest_whenCreatingInstrument_thenReturnsCreatedStatus() throws Exception {
        String requestJson = """
                {"ticker":"SGLN.L","name":"iShares Gold","currency":"GBP","targetWeightPct":10.0}
                """;

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    void givenUnknownTicker_whenDeletingInstrument_thenReturnsNotFoundStatus() throws Exception {
        doThrow(new NoSuchElementException("Instrument not found: UNKNOWN"))
                .when(instrumentService).deleteInstrument("UNKNOWN");

        mockMvc.perform(delete("/api/instruments/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Instrument not found: UNKNOWN")));
    }
}