package com.example.parkinglotattendanthelper.core;

import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.common.VehicleType;

public interface IManageParkingLots {
    ParkingLotSummary generateParkingLotSummary(String parkingLotId);

    String reserveParkingAvailability(String parkingLotId, VehicleType parkingVehicle);

    void admitVehicleForParking(String parkingReservationId) throws Exception;
}
