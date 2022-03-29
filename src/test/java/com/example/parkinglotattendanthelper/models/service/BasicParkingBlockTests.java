package com.example.parkinglotattendanthelper.models.service;

import com.example.parkinglotattendanthelper.models.common.VehicleType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BasicParkingBlockTests {
    private static Stream<Arguments> provideVehicleTypes() {
        return Stream.of(
                Arguments.of(VehicleType.CAR),
                Arguments.of(VehicleType.MONSTERTRUCK)
        );
    }

    @ParameterizedTest
    @MethodSource("provideVehicleTypes")
    void defaultParkingBlockShouldHaveNoSpacesAvailable(VehicleType parkingCar){
        var parkingBlock = BasicParkingBlock.builder().build();

        assertFalse(parkingBlock.hasSpaceFor(parkingCar));
    }

    private static Stream<Arguments> provideVehicleTypesAndBuiltParkingBlock() {
        return Stream.of(
                Arguments.of(null, VehicleType.CAR, false),
                Arguments.of(StringUtils.EMPTY, VehicleType.CAR, false),
                Arguments.of("ABCDEFG", VehicleType.CAR, true),
                Arguments.of("__", VehicleType.CAR, true),
                Arguments.of("_MT", VehicleType.CAR, true),
                Arguments.of("MT_", VehicleType.CAR, true),
                Arguments.of("MT", VehicleType.CAR, false),
                Arguments.of("MT!", VehicleType.CAR, false),
                Arguments.of("!MT", VehicleType.CAR, false),
                Arguments.of("__", VehicleType.MONSTERTRUCK, true),
                Arguments.of("MT_", VehicleType.MONSTERTRUCK, false),
                Arguments.of("_MT", VehicleType.MONSTERTRUCK, false),
                Arguments.of("_!", VehicleType.MONSTERTRUCK, false),
                Arguments.of("!_", VehicleType.MONSTERTRUCK, false),
                Arguments.of("MTMT", VehicleType.MONSTERTRUCK, false),
                Arguments.of("_____", VehicleType.MONSTERTRUCK, true),
                Arguments.of("MT___", VehicleType.MONSTERTRUCK, true),
                Arguments.of("MT__MT_", VehicleType.MONSTERTRUCK, true),
                Arguments.of("__!__", VehicleType.MONSTERTRUCK, true),
                Arguments.of("__MT__", VehicleType.MONSTERTRUCK, true),
                Arguments.of("MTMTMT__", VehicleType.MONSTERTRUCK, true),
                Arguments.of("MT!MT__", VehicleType.MONSTERTRUCK, true),
                Arguments.of("_!MT_!", VehicleType.MONSTERTRUCK, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideVehicleTypesAndBuiltParkingBlock")
    void builtParkingBlockShouldHaveNoSpacesAvailable(String parkingSpaces, VehicleType parkingCar, boolean expectedResult){
        var parkingBlock = BasicParkingBlock.builder()
                .parkingSpaces(parkingSpaces)
                .build();

        assertEquals(expectedResult, parkingBlock.hasSpaceFor(parkingCar));
    }

    private static Stream<Arguments> provideBuiltParkingBlock() {
        return Stream.of(
                Arguments.of(null, Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of(StringUtils.EMPTY, Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("__", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("ABCDEFG", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 1)),
                Arguments.of("_MT", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("MT_", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("MT", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("MT!", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("!MT", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("__", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("MT_", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("_MT", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 0)),
                Arguments.of("_!", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("!_", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("MTMT", Map.of(VehicleType.MONSTERTRUCK, 2, VehicleType.CAR, 0)),
                Arguments.of("CCMT_", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 2)),
                Arguments.of("MT_C_", Map.of(VehicleType.MONSTERTRUCK, 1, VehicleType.CAR, 1)),
                Arguments.of("MT_CMT_", Map.of(VehicleType.MONSTERTRUCK, 2, VehicleType.CAR, 1)),
                Arguments.of("__!__", Map.of(VehicleType.MONSTERTRUCK, 0, VehicleType.CAR, 0)),
                Arguments.of("MTMTMT_!", Map.of(VehicleType.MONSTERTRUCK, 3, VehicleType.CAR, 0)),
                Arguments.of("MT!MT__", Map.of(VehicleType.MONSTERTRUCK, 2, VehicleType.CAR, 0)),
                Arguments.of("_!MT_!", Map.of(VehicleType.CAR, 0, VehicleType.MONSTERTRUCK, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("provideBuiltParkingBlock")
    void builtParkingBlockShouldHaveNoSpacesAvailable(String parkingSpaces, Map<VehicleType, Integer> expectedResult){
        var parkingBlock = BasicParkingBlock.builder()
                .parkingSpaces(parkingSpaces)
                .build();

        var result = parkingBlock.getParkedVehicles();

        assertEquals(expectedResult, parkingBlock.getParkedVehicles());
    }
}
