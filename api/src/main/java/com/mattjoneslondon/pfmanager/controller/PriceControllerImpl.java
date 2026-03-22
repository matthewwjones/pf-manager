package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.dto.LoadPricesRequest;
import com.mattjoneslondon.pfmanager.repository.EomPriceRepository;
import com.mattjoneslondon.pfmanager.service.PriceLoaderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceControllerImpl implements PriceController {

    private final EomPriceRepository eomPriceRepository;
    private final PriceLoaderService priceLoaderService;

    public PriceControllerImpl(EomPriceRepository eomPriceRepository, PriceLoaderService priceLoaderService) {
        this.eomPriceRepository = eomPriceRepository;
        this.priceLoaderService = priceLoaderService;
    }

    @Override
    @GetMapping
    public List<EomPrice> getAllPrices() {
        return eomPriceRepository.findAll();
    }

    @Override
    @GetMapping("/{ticker}")
    public List<EomPrice> getPricesForTicker(@PathVariable String ticker) {
        return eomPriceRepository.findByTicker(ticker);
    }

    @Override
    @PostMapping("/load")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void loadPrices(@RequestBody(required = false) LoadPricesRequest request) {
        LocalDate eomDate = request != null && request.date() != null
                ? request.date()
                : YearMonth.now().atEndOfMonth();
        priceLoaderService.loadPricesForDate(eomDate);
    }
}
