package com.example.parkinglotattendanthelper.core;

import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.common.VehicleType;

public interface IManageParkingLots {
    /**
     * Generate a parking lot summary based on the current state of the lot itself
     * (vehicle types/counts, revenue collected)
     * @param parkingLotId  The unique id used to query for parking lot data.
     * @return              The generated summary report.
     * @see ParkingLotSummary
     */
    ParkingLotSummary generateParkingLotSummary(String parkingLotId);

    /**
     * Checks the parking lot data for the given lot id to determine if there
     * is space for the given vehicleReserve. If space exists, it is "reserved"
     * and a reservation id is generated and returned.
     * @param parkingLotId      The unique id used to interact with the parking lot data
     * @param parkingVehicle    The type of vehicle requesting parking
     * @return                  String representing the constructed reservation id.
     */
    String reserveParkingAvailability(String parkingLotId, VehicleType parkingVehicle);

    /**
     * Records the vehicle associated with the given reservation id as "parked".
     * @param parkingReservationId  The unique reservation id to transact on.
     * @throws Exception
     */
    void admitVehicleForParking(String parkingReservationId) throws Exception;
}
