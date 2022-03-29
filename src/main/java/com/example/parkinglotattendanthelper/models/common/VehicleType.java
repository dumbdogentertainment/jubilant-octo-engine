package com.example.parkinglotattendanthelper.models.common;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum VehicleType {
    CAR("CAR", 1),
    MONSTERTRUCK("MONSTERTRUCK", 2),
    UNSUPPORTED("UNSUPPORTED", 0),
    NONE("NONE", 0);

    private final String asString;
    private final int spacesNeeded;

    VehicleType(String asString, int spacesNeeded) {
        this.asString = asString;
        this.spacesNeeded = spacesNeeded;
    }

    public static VehicleType valueOfOrDefault(String incomingValue) {
        try {
            return VehicleType.valueOf(incomingValue.toUpperCase().replace("-", StringUtils.EMPTY));
        } catch (IllegalArgumentException caughtException) {
            return VehicleType.UNSUPPORTED;
        }
    }
}
