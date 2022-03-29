package com.example.parkinglotattendanthelper.services;

import com.example.parkinglotattendanthelper.core.IProvideParkingLotData;
import com.example.parkinglotattendanthelper.exceptions.BadRequestException;
import com.example.parkinglotattendanthelper.exceptions.ParkingLotFullException;
import com.example.parkinglotattendanthelper.exceptions.ParkingLotNotInServiceException;
import com.example.parkinglotattendanthelper.exceptions.ReservationNotFoundException;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.example.parkinglotattendanthelper.models.service.BasicParkingBlock;
import com.example.parkinglotattendanthelper.models.service.BasicParkingLot;
import com.flextrade.jfixture.JFixture;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BasicParkingLotManagerTests {
    final JFixture fixture = new JFixture();

    IProvideParkingLotData mockProvider = mock(IProvideParkingLotData.class);

    BasicParkingLotManager systemUnderTest;

    @BeforeAll
    void init() {
        this.systemUnderTest = new BasicParkingLotManager(this.mockProvider);
    }

    @Test
    void basicParkingLotManagerShouldGenerateSummaryAsExpected() {
        var blocks = Map.of(
                "block1", BasicParkingBlock.builder().parkingSpaces("____").build(),
                "block2", BasicParkingBlock.builder().parkingSpaces("_MT_").build(),
                "block3", BasicParkingBlock.builder().parkingSpaces("!C__").build(),
                "block4", BasicParkingBlock.builder().parkingSpaces("!C__").build(),
                "block5", BasicParkingBlock.builder().parkingSpaces("!CMTMTC").build(),
                "block6", BasicParkingBlock.builder().parkingSpaces("MTC!").build());

        var basicParkingLot = BasicParkingLot.builder()
                .parkingLotRates(Map.of(VehicleType.CAR, 5d, VehicleType.MONSTERTRUCK, 15d))
                .parkingLotBlocks(blocks)
                .build();

        when(this.mockProvider.retrieveParkingLotData(anyString())).thenReturn(basicParkingLot);

        var summary = this.systemUnderTest.generateParkingLotSummary(this.fixture.create(String.class));

        assertEquals(2, summary.getParkedVehicleCounts().size());
        assertEquals(5, summary.getParkedVehicleCounts().get(VehicleType.CAR));
        assertEquals(4, summary.getParkedVehicleCounts().get(VehicleType.MONSTERTRUCK));

        assertEquals(2, summary.getParkedVehicleRates().size());
        assertEquals(5, summary.getParkedVehicleRates().get(VehicleType.CAR));
        assertEquals(15, summary.getParkedVehicleRates().get(VehicleType.MONSTERTRUCK));

        // number of CARs * CAR rate + number of MONSTERTRUCKs * MONSTERTRUCK rate
        assertEquals(5 * 5 + 4 * 15, summary.getTotalRevenue());
    }

    @Test
    void basicParkingLotManagerShouldThrowExceptionForLotWithNoRates() {
        var basicParkingLot = BasicParkingLot.builder().build();

        when(this.mockProvider.retrieveParkingLotData(anyString())).thenReturn(basicParkingLot);

        assertThrows(
                ParkingLotNotInServiceException.class,
                () -> this.systemUnderTest.generateParkingLotSummary(this.fixture.create(String.class)));
    }

    private static Stream<Arguments> provideVehicleTypesForReservation() {
        return Stream.of(
                Arguments.of(VehicleType.CAR),
                Arguments.of(VehicleType.MONSTERTRUCK)
        );
    }

    @ParameterizedTest
    @MethodSource("provideVehicleTypesForReservation")
    void parkingReservationYieldsConfirmationId(VehicleType incomingVehicle) {
        var mockConfirmationId = this.fixture.create(String.class);
        when(this.mockProvider.reserveParkingSpace(anyString(), any(VehicleType.class)))
                .thenReturn(mockConfirmationId);

        assertEquals(
                Base64.getEncoder().encodeToString(mockConfirmationId.getBytes(StandardCharsets.UTF_8)),
                this.systemUnderTest.reserveParkingAvailability(
                        this.fixture.create(String.class),
                        incomingVehicle));
    }

    private static Stream<Arguments> provideExceptionsThrownForParkingReservation() {
        return Stream.of(
                Arguments.of(new BadRequestException("For testing ..")),
                Arguments.of(new ParkingLotFullException("For testing .."))
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsThrownForParkingReservation")
    void parkingReservationYieldsThrownException(Exception thrownException) {
        when(this.mockProvider.reserveParkingSpace(anyString(), any(VehicleType.class)))
                .thenThrow(thrownException);

        assertThrows(
                thrownException.getClass(),
                () -> this.systemUnderTest.reserveParkingAvailability(
                        this.fixture.create(String.class),
                        VehicleType.CAR));
        assertThrows(
                thrownException.getClass(),
                () -> this.systemUnderTest.reserveParkingAvailability(
                        this.fixture.create(String.class),
                        VehicleType.MONSTERTRUCK));
    }

    @Test
    void parkingReservationForUnsupportedVehicleYieldsException() {
        assertThrows(
                BadRequestException.class,
                () -> this.systemUnderTest.reserveParkingAvailability(
                        this.fixture.create(String.class),
                        VehicleType.UNSUPPORTED));
    }

    @Test
    void parkingReservationConfirmationYieldsReservationNotFound() {
        doThrow(new ReservationNotFoundException())
                .when(this.mockProvider)
                .admitVehicle(anyString());

        assertThrows(
                ReservationNotFoundException.class,
                () -> this.systemUnderTest.admitVehicleForParking(this.fixture.create(String.class)));
    }
}
