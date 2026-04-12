package com.mattjoneslondon.pfmanager.service.holding;

import com.mattjoneslondon.pfmanager.dao.holding.HoldingRepository;
import com.mattjoneslondon.pfmanager.domain.holding.Holding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;

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