package com.example.parkinglotattendanthelper.models.service;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BasicParkingLotTests {
    @Test
    void blocksOfParkingSpacesShouldMapToParkedVehicles(){
        var blocks = Map.of(
                "block0", BasicParkingBlock.builder().parkingSpaces(StringUtils.EMPTY).build(),
                "block1", BasicParkingBlock.builder().parkingSpaces("____").build(),
                "block2", BasicParkingBlock.builder().parkingSpaces("_MT_").build(),
                "block3", BasicParkingBlock.builder().parkingSpaces("!C__").build(),
                "block4", BasicParkingBlock.builder().parkingSpaces("MTC!").build());

        var parkingLot = BasicParkingLot.builder()
                .parkingLotBlocks(blocks)
                .build();

        var result = parkingLot.getParkedVehicles();

        assertFalse(result.keySet().isEmpty());
        assertEquals(2, result.get(VehicleType.CAR));
        assertEquals(2, result.get(VehicleType.MONSTERTRUCK));
    }
}
