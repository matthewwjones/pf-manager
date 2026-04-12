package com.mattjoneslondon.pfmanager.service.instrument;

import com.mattjoneslondon.pfmanager.dao.instrument.InstrumentRepository;
import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

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