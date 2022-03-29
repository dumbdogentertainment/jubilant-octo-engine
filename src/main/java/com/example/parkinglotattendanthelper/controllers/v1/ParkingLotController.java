package com.example.parkinglotattendanthelper.controllers.v1;

import com.example.parkinglotattendanthelper.core.IManageParkingLots;
import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.api.ParkingReservationRequest;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/parking-lots")
public class ParkingLotController {
    private final IManageParkingLots parkingLotManagerService;

    @Operation(summary = "Generate and present a summary of parked vehicle counts and the total revenue collected.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Corresponding parking lot id exists for given id",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParkingLotSummary.class))}),
            @ApiResponse(responseCode = "404", description = "Parking lot not found for given id", content = @Content),
            @ApiResponse(responseCode = "501", description = "Parking lot not in service yet", content = @Content)
    })
    @GetMapping(value = "/{parkingLotId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParkingLotSummary> retrieveParkingLotSummary(
            @Parameter(description = "Id of the parking lot to generate summary for")
            @PathVariable String parkingLotId) {
        var summary = this.parkingLotManagerService.generateParkingLotSummary(parkingLotId);

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Submit new request to park .. if successful, the response header 'Location' URI is to be used to admit the vehicle into the lot.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Parking lot has space for the given vehicle and the reservation request was created",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Vehicle in payload is not supported", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parking lot has no spaces to accommodate given vehicle", content = @Content),
            @ApiResponse(responseCode = "404", description = "Parking lot not found for given id", content = @Content)
    })
    @PostMapping()
    public ResponseEntity<Void> reserveParkingSpace(
            @Parameter(description = "Payload containing the type of vehicle requesting parking and the parking lot id")
            @RequestBody ParkingReservationRequest incomingRequest,
            HttpServletRequest httpRequest) {
        var reservationId = this.parkingLotManagerService.reserveParkingAvailability(
                incomingRequest.getParkingLotId(),
                VehicleType.valueOfOrDefault(incomingRequest.getVehicleType()));
        var confirmationUri = httpRequest.getRequestURI() + "/" + reservationId;

        return ResponseEntity
                .created(URI.create(confirmationUri))
                .build();
    }

    @Operation(summary = "Confirm an existing request to park .. admit the vehicle to park.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Vehicle is admitted to park (available space was previously confirmed) and the vehicle will reflect in the lot summary",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Given parking reservation record not found", content = @Content)
    })
    @PatchMapping(value = "/{parkingReservationId}")
    public ResponseEntity<Void> admitVehicleToParkingLot(@PathVariable String parkingReservationId) throws Exception {
        this.parkingLotManagerService.admitVehicleForParking(parkingReservationId);

        return ResponseEntity.accepted().build();
    }
}
