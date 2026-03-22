package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.EomPrice;
import com.mattjoneslondon.pfmanager.dto.LoadPricesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Prices", description = "Manage end-of-month closing prices for portfolio instruments")
public interface PriceController {

    @Operation(summary = "Get all prices", description = "Returns every stored end-of-month closing price across all instruments.")
    @ApiResponse(responseCode = "200", description = "List of all EOM prices",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EomPrice.class))))
    List<EomPrice> getAllPrices();

    @Operation(summary = "Get prices for a ticker", description = "Returns all stored end-of-month closing prices for the given instrument.")
    @ApiResponse(responseCode = "200", description = "EOM prices for the specified ticker",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EomPrice.class))))
    List<EomPrice> getPricesForTicker(
            @Parameter(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker);

    @Operation(
            summary = "Load prices from EODHD",
            description = "Triggers loading of end-of-month closing prices from EODHD for all instruments. " +
                    "Defaults to the end of the current month when no date is supplied."
    )
    @ApiResponse(responseCode = "202", description = "Price load request accepted")
    void loadPrices(
            @RequestBody(required = false, description = "Date to load prices for. Omit to use end of current month.",
                    content = @Content(schema = @Schema(implementation = LoadPricesRequest.class))) LoadPricesRequest request);
}