package com.example.parkinglotattendanthelper.core;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.example.parkinglotattendanthelper.models.service.BasicParkingLot;

public interface IProvideParkingLotData {
    BasicParkingLot retrieveParkingLotData(String parkingLotId);

    String reserveParkingSpace(String parkingLotId, VehicleType vehicleType);

    void admitVehicle(String parkingReservationId);

    void resetParkingLots();
}
