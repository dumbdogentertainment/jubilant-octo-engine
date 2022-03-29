package com.example.parkinglotattendanthelper.models.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ParkingReservationRequest {
    String parkingLotId;
    String vehicleType;
}
