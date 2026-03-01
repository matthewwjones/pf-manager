package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.Instrument;
import com.mattjoneslondon.pfmanager.dto.InstrumentRequest;
import com.mattjoneslondon.pfmanager.service.InstrumentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public List<Instrument> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    @GetMapping("/{ticker}")
    public Instrument getInstrument(@PathVariable String ticker) {
        return instrumentService.getInstrument(ticker);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInstrument(@RequestBody InstrumentRequest request) {
        Instrument instrument = new Instrument(
                request.ticker(), request.name(), request.currency(), request.targetWeightPct()
        );
        instrumentService.saveInstrument(instrument);
    }

    @PutMapping("/{ticker}")
    public void updateInstrument(@PathVariable String ticker, @RequestBody InstrumentRequest request) {
        Instrument instrument = new Instrument(
                ticker, request.name(), request.currency(), request.targetWeightPct()
        );
        instrumentService.saveInstrument(instrument);
    }

    @DeleteMapping("/{ticker}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable String ticker) {
        instrumentService.deleteInstrument(ticker);
    }
}
