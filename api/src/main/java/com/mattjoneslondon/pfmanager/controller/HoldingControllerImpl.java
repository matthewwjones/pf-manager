package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.Holding;
import com.mattjoneslondon.pfmanager.dto.HoldingRequest;
import com.mattjoneslondon.pfmanager.service.HoldingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
public class HoldingControllerImpl implements HoldingController {

    private final HoldingService holdingService;

    public HoldingControllerImpl(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @Override
    @GetMapping
    public List<Holding> getAllHoldings() {
        return holdingService.getAllHoldings();
    }

    @Override
    @GetMapping("/{ticker}")
    public List<Holding> getHoldingsForTicker(@PathVariable String ticker) {
        return holdingService.getHoldingsForTicker(ticker);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createHolding(@RequestBody HoldingRequest request) {
        Holding holding = new Holding(0, request.ticker(), request.shares(), request.effectiveDate());
        holdingService.saveHolding(holding);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHolding(@PathVariable long id) {
        holdingService.deleteHolding(id);
    }
}
