package com.example.parkinglotattendanthelper.core;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.example.parkinglotattendanthelper.models.service.BasicParkingLot;

public interface IProvideParkingLotData {
    /**
     * Retrieve parking lot data for the given unique lot id.
     * @param parkingLotId  The given parking lot id to be used to query.
     * @return              The retrieved parking lot data.
     * @see BasicParkingLot
     */
    BasicParkingLot retrieveParkingLotData(String parkingLotId);

    /**
     * Reserve a parking fitting the specified vehicle type in the parking lot
     * matching the given parking lot id.
     * @param parkingLotId  The given parking lot id to be used.
     * @param vehicleType   The type of vehicle attempting to park.
     * @return              The id of the created reservation.
     */
    String reserveParkingSpace(String parkingLotId, VehicleType vehicleType);

    /**
     * "Confirm" the reservation for the given reservation id and admit the
     * vehicle for parking.
     * @param parkingReservationId  The given reservation id to confirm.
     */
    void admitVehicle(String parkingReservationId);

    /**
     * Purely a testing function to be used in current test runs. Ideally,
     * a more robust instance of persistence would be established that would
     * accommodate resetting of data for testing purposes.
     */
    void resetParkingLots();
}
