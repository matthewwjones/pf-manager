package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.Instrument;
import com.mattjoneslondon.pfmanager.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    public Instrument getInstrument(String ticker) {
        return instrumentRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoSuchElementException("Instrument not found: " + ticker));
    }

    public void saveInstrument(Instrument instrument) {
        instrumentRepository.save(instrument);
    }

    public void deleteInstrument(String ticker) {
        instrumentRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoSuchElementException("Instrument not found: " + ticker));
        instrumentRepository.deleteByTicker(ticker);
    }
}
