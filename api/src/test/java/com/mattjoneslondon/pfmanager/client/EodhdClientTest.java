package com.mattjoneslondon.pfmanager.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class EodhdClientTest {

    private static final String API_KEY = "test-api-key";
    private static final LocalDate FROM = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO = LocalDate.of(2024, 1, 31);

    private MockWebServer mockServer;
    private EodhdClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        client = new EodhdClient(mockServer.url("/").toString(), API_KEY);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void fetchMonthlyPricesReturnsParsedRecords() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("""
                        [{"date":"2024-01-31","open":100.0,"high":110.0,"low":90.0,
                          "close":105.0,"adjusted_close":104.5,"volume":1000000}]
                        """)
                .addHeader("Content-Type", "application/json"));

        final List<EodhdPriceRecord> records = client.fetchMonthlyPrices("AAPL.US", FROM, TO);
        final EodhdPriceRecord priceRecord = records.get(0);

        assertAll(
                () -> assertThat(records, hasSize(1)),
                () -> assertThat(priceRecord.date(), is("2024-01-31")),
                () -> assertThat(priceRecord.open(), is(100.0)),
                () -> assertThat(priceRecord.high(), is(110.0)),
                () -> assertThat(priceRecord.low(), is(90.0)),
                () -> assertThat(priceRecord.close(), is(105.0)),
                () -> assertThat(priceRecord.adjustedClose(), is(104.5)),
                () -> assertThat(priceRecord.volume(), is(1000000L))
        );
    }

    @Test
    void fetchMonthlyPricesReturnsEmptyListWhenResponseIsEmptyArray() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        final List<EodhdPriceRecord> records = client.fetchMonthlyPrices("AAPL.US", FROM, TO);

        assertThat(records, is(empty()));
    }

    @Test
    void fetchMonthlyPricesSendsCorrectQueryParameters() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        client.fetchMonthlyPrices("AAPL.US", FROM, TO);

        final RecordedRequest request = mockServer.takeRequest();
        final String path = request.getPath();
        assertAll(
                () -> assertThat(path, containsString("/eod/AAPL.US")),
                () -> assertThat(path, containsString("api_token=" + API_KEY)),
                () -> assertThat(path, containsString("from=2024-01-01")),
                () -> assertThat(path, containsString("to=2024-01-31")),
                () -> assertThat(path, containsString("period=m")),
                () -> assertThat(path, containsString("fmt=json"))
        );
    }

    @Test
    void fetchMonthlyExchangeRatesUsesTheSameEndpoint() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("""
                        [{"date":"2024-01-31","open":1.27,"high":1.28,"low":1.26,
                          "close":1.27,"adjusted_close":1.27,"volume":0}]
                        """)
                .addHeader("Content-Type", "application/json"));

        final List<EodhdPriceRecord> records = client.fetchMonthlyExchangeRates("GBPUSD.FOREX", FROM, TO);

        final RecordedRequest request = mockServer.takeRequest();
        assertAll(
                () -> assertThat(records, hasSize(1)),
                () -> assertThat(request.getPath(), containsString("/eod/GBPUSD.FOREX"))
        );
    }
}