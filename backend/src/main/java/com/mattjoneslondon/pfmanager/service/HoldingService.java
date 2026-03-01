package com.mattjoneslondon.pfmanager.service;

import com.mattjoneslondon.pfmanager.domain.Holding;
import com.mattjoneslondon.pfmanager.repository.HoldingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<Holding> getAllHoldings() {
        return holdingRepository.findAll();
    }

    public List<Holding> getHoldingsForTicker(String ticker) {
        return holdingRepository.findByTicker(ticker);
    }

    public void saveHolding(Holding holding) {
        holdingRepository.upsert(holding);
    }

    public void deleteHolding(long id) {
        holdingRepository.deleteById(id);
    }
}
