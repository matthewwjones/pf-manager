package com.mattjoneslondon.pfmanager.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class EodhdClient {
    private static final String MONTHLY_PERIOD = "m";
    private static final String JSON_FORMAT = "json";
    private final RestClient restClient;
    private final String apiKey;

    public EodhdClient(@Value("${eodhd.base-url}") String baseUrl,
                       @Value("${eodhd.api-key}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<EodhdPriceRecord> fetchMonthlyPrices(String ticker, LocalDate from, LocalDate to) {
        log.info("Requesting monthly prices from EODHD: ticker={}, from={}, to={}", ticker, from, to);
        final EodhdPriceRecord[] records = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eod/{ticker}")
                        .queryParam("api_token", apiKey)
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .queryParam("period", MONTHLY_PERIOD)
                        .queryParam("fmt", JSON_FORMAT)
                        .build(ticker))
                .retrieve()
                .body(EodhdPriceRecord[].class);
        return records != null ? List.of(records) : List.of();
    }

    public List<EodhdPriceRecord> fetchMonthlyExchangeRates(String forexTicker, LocalDate from, LocalDate to) {
        return fetchMonthlyPrices(forexTicker, from, to);
    }
}
