package com.example.parkinglotattendanthelper.models.api;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLotSummary {
    @Builder.Default
    Map<VehicleType, Integer> parkedVehicleCounts = new HashMap<>();

    @Builder.Default
    Map<VehicleType, Double> parkedVehicleRates = new HashMap<>();

    double totalRevenue;

    public double getTotalRevenue() {
        return (this.parkedVehicleCounts.keySet().stream()
                .map(key -> this.parkedVehicleCounts.get(key) * this.parkedVehicleRates.get(key))
                .collect(Collectors.toList())).stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
