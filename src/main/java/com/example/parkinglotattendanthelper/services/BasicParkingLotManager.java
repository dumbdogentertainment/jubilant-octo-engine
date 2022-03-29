package com.example.parkinglotattendanthelper.services;

import com.example.parkinglotattendanthelper.core.IManageParkingLots;
import com.example.parkinglotattendanthelper.core.IProvideParkingLotData;
import com.example.parkinglotattendanthelper.exceptions.BadRequestException;
import com.example.parkinglotattendanthelper.exceptions.ParkingLotNotInServiceException;
import com.example.parkinglotattendanthelper.exceptions.ReservationNotFoundException;
import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class BasicParkingLotManager implements IManageParkingLots {
    private final IProvideParkingLotData parkingLotDataProvider;

    @Override
    public ParkingLotSummary generateParkingLotSummary(String parkingLotId) {
        var parkingLotData = this.parkingLotDataProvider.retrieveParkingLotData(parkingLotId);

        if (parkingLotData.getParkingLotRates().isEmpty()) {
            throw new ParkingLotNotInServiceException("This parking lot is not open yet.");
        }

        return ParkingLotSummary.builder()
                .parkedVehicleRates(parkingLotData.getParkingLotRates())
                .parkedVehicleCounts(parkingLotData.getParkedVehicles())
                .build();
    }

    @Override
    public String reserveParkingAvailability(String parkingLotId, VehicleType parkingVehicle) {
        if(parkingVehicle.equals(VehicleType.UNSUPPORTED)){
            throw new BadRequestException("Unsupported vehicle.");
        }

        var confirmationId = this.parkingLotDataProvider.reserveParkingSpace(parkingLotId, parkingVehicle);
        return Base64.getEncoder().encodeToString(confirmationId.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void admitVehicleForParking(String parkingReservationId) throws IOException {
        try {
            var decodedReservationId = new String(Base64.getDecoder().decode(parkingReservationId), StandardCharsets.UTF_8);
            this.parkingLotDataProvider.admitVehicle(decodedReservationId);
        } catch (IllegalArgumentException caughtException){
            // don't tip off that the provided encoded id is invalid
            throw new ReservationNotFoundException("Reservation not found ..");
        }
    }
}
