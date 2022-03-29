package com.example.parkinglotattendanthelper.models.service;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Builder
@Getter
public class BasicParkingLot {
    @Builder.Default
    String parkingLotId = StringUtils.EMPTY;

    @Builder.Default
    Map<VehicleType, Double> parkingLotRates = Collections.emptyMap();

    @Builder.Default
    Map<String, BasicParkingBlock> parkingLotBlocks = Collections.emptyMap();

    public Map<VehicleType, Integer> getParkedVehicles() {
        // this could be cleaner
        var set = new HashMap<VehicleType, Integer>();
        this.parkingLotBlocks.values().forEach(val -> {
            val.getParkedVehicles().entrySet().forEach(valp -> {
                set.putIfAbsent(valp.getKey(), 0);
                set.replace(valp.getKey(), set.get(valp.getKey()) + valp.getValue());
            });
        });

        return set;
    }
}
