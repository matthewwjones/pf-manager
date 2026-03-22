package com.mattjoneslondon.pfmanager.controller;

import com.mattjoneslondon.pfmanager.domain.Holding;
import com.mattjoneslondon.pfmanager.dto.HoldingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Holdings", description = "Manage the shares held in the portfolio")
public interface HoldingController {

    @Operation(summary = "Get all holdings", description = "Returns every holding record across all instruments.")
    @ApiResponse(responseCode = "200", description = "List of all holdings",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Holding.class))))
    List<Holding> getAllHoldings();

    @Operation(summary = "Get holdings for a ticker", description = "Returns all holding records for the given instrument.")
    @ApiResponse(responseCode = "200", description = "Holdings for the specified ticker",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Holding.class))))
    List<Holding> getHoldingsForTicker(
            @Parameter(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker);

    @Operation(summary = "Create a holding", description = "Records a new share holding for an instrument on the given effective date.")
    @ApiResponse(responseCode = "201", description = "Holding created successfully")
    void createHolding(
            @RequestBody(description = "Holding details",
                    content = @Content(schema = @Schema(implementation = HoldingRequest.class))) HoldingRequest request);

    @Operation(summary = "Delete a holding", description = "Removes the holding record with the given ID.")
    @ApiResponse(responseCode = "204", description = "Holding deleted successfully")
    void deleteHolding(@Parameter(description = "Holding ID", example = "1") long id);
}