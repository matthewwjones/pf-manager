package com.mattjoneslondon.pfmanager.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EodhdPriceRecord(
        @JsonProperty("date") String date,
        @JsonProperty("open") double open,
        @JsonProperty("high") double high,
        @JsonProperty("low") double low,
        @JsonProperty("close") double close,
        @JsonProperty("adjusted_close") double adjustedClose,
        @JsonProperty("volume") long volume
) {
}
