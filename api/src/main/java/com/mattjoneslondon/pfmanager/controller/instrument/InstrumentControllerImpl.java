package com.mattjoneslondon.pfmanager.controller.instrument;

import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import com.mattjoneslondon.pfmanager.domain.instrument.InstrumentRequest;
import com.mattjoneslondon.pfmanager.service.instrument.InstrumentService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class InstrumentControllerImpl implements InstrumentController {

    private final InstrumentService instrumentService;

    @Override
    @GetMapping
    public List<Instrument> getAllInstruments() {
        return instrumentService.getAllInstruments();
    }

    @Override
    @GetMapping("/{ticker}")
    public Instrument getInstrument(@PathVariable String ticker) {
        return instrumentService.getInstrument(ticker);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInstrument(@RequestBody InstrumentRequest request) {
        final Instrument instrument = new Instrument(request.ticker(), request.name(),
                                                     request.currency(), request.targetWeightPct());
        instrumentService.saveInstrument(instrument);
    }

    @Override
    @PutMapping("/{ticker}")
    public void updateInstrument(@PathVariable String ticker, @RequestBody InstrumentRequest request) {
        final Instrument instrument = new Instrument(ticker, request.name(),
                                                     request.currency(), request.targetWeightPct());
        instrumentService.saveInstrument(instrument);
    }

    @Override
    @DeleteMapping("/{ticker}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable String ticker) {
        instrumentService.deleteInstrument(ticker);
    }
}