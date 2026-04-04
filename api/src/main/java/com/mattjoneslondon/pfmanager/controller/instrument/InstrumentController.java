package com.mattjoneslondon.pfmanager.controller.instrument;

import com.mattjoneslondon.pfmanager.domain.instrument.Instrument;
import com.mattjoneslondon.pfmanager.domain.instrument.InstrumentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Instruments", description = "Manage the investable instruments tracked in the portfolio")
public interface InstrumentController {

    @Operation(summary = "Get all instruments", description = "Returns every instrument currently tracked in the portfolio.")
    @ApiResponse(responseCode = "200", description = "List of all instruments",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Instrument.class))))
    List<Instrument> getAllInstruments();

    @Operation(summary = "Get an instrument by ticker")
    @ApiResponse(responseCode = "200", description = "The requested instrument",
            content = @Content(schema = @Schema(implementation = Instrument.class)))
    Instrument getInstrument(
            @Parameter(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker);

    @Operation(summary = "Create an instrument", description = "Adds a new instrument to the portfolio with a target weight.")
    @ApiResponse(responseCode = "201", description = "Instrument created successfully")
    void createInstrument(
            @RequestBody(description = "Instrument details",
                    content = @Content(schema = @Schema(implementation = InstrumentRequest.class))) InstrumentRequest request);

    @Operation(summary = "Update an instrument", description = "Replaces the stored details for the given instrument.")
    @ApiResponse(responseCode = "200", description = "Instrument updated successfully")
    void updateInstrument(
            @Parameter(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker,
            @RequestBody(description = "Updated instrument details",
                    content = @Content(schema = @Schema(implementation = InstrumentRequest.class))) InstrumentRequest request);

    @Operation(summary = "Delete an instrument")
    @ApiResponse(responseCode = "204", description = "Instrument deleted successfully")
    void deleteInstrument(
            @Parameter(description = "Instrument ticker symbol", example = "VWRL.LSE") String ticker);
}
