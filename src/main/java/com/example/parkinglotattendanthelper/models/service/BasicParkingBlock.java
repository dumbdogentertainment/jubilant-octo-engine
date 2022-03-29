package com.example.parkinglotattendanthelper.models.service;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
@Builder
public class BasicParkingBlock {
    @Builder.Default
    String parkingBlockId = StringUtils.EMPTY;

    @Builder.Default
    String parkingSpaces = StringUtils.EMPTY;

    public String getParkingSpaces() {
        return Optional.ofNullable(parkingSpaces).orElse(StringUtils.EMPTY);
    }

    public boolean hasSpaceFor(VehicleType incomingVehicle) {
        // split on C or MT (which denotes taken) and then again on ! (which denotes reserved)
        var adjacentAndAvailable = String
                .join(StringUtils.EMPTY, this.getParkingSpaces().split("C|MT"))
                .split("!");

        return Arrays.stream(adjacentAndAvailable)
                .anyMatch(group -> group.length() >= incomingVehicle.getSpacesNeeded());
    }

    public Map<VehicleType, Integer> getParkedVehicles(){
        return Map.of(
                VehicleType.CAR, StringUtils.countMatches(this.getParkingSpaces(), "C"),
                VehicleType.MONSTERTRUCK, StringUtils.countMatches(this.getParkingSpaces(), "MT")
        );
    }
}
