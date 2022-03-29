package com.example.parkinglotattendanthelper.controllers.v1;

import com.example.parkinglotattendanthelper.controllers.CustomErrorAdvice;
import com.example.parkinglotattendanthelper.controllers.v1.ParkingLotController;
import com.example.parkinglotattendanthelper.core.IManageParkingLots;
import com.example.parkinglotattendanthelper.exceptions.*;
import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.api.ParkingReservationRequest;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ParkingLotControllerTests {
    final JFixture fixture = new JFixture();
    final ObjectMapper mapper = new ObjectMapper();

    IManageParkingLots mockParkingLotManager = mock(IManageParkingLots.class);

    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        var systemUnderTest = new ParkingLotController(this.mockParkingLotManager);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(systemUnderTest)
                .setControllerAdvice(new CustomErrorAdvice())
                .alwaysDo(print())
                .build();
    }

    private static Stream<Arguments> provideCountsForMockGeneratedSummary() {
        return Stream.of(
                Arguments.of(3, 5.0, 5, 15.0, 3 * 5.0 + 5 * 15.0),
                Arguments.of(0, 5.0, 0, 15.0, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCountsForMockGeneratedSummary")
    void summaryRetrieveShouldRespondWithGeneratedSummary(
            int numberOfCars,
            double carRate,
            int numberOfTrucks,
            double truckRate,
            double expectedTotalRevenue) throws Exception {
        when(this.mockParkingLotManager.generateParkingLotSummary(anyString()))
                .thenReturn(this.buildMockSummary(
                        numberOfCars,
                        carRate,
                        numberOfTrucks,
                        truckRate));

        var response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/parking-lots/" + this.fixture.create(String.class)))
                .andExpect(status().isOk())
                .andReturn();

        var deserialized = this.mapper.readValue(response.getResponse().getContentAsString(), ParkingLotSummary.class);
        assertEquals(expectedTotalRevenue, deserialized.getTotalRevenue());
    }

    private static Stream<Arguments> provideExceptionsThrownForParkingReservation() {
        return Stream.of(
                Arguments.of(new BadRequestException(), status().isBadRequest()),
                Arguments.of(new BadRequestException("For testing .."), status().isBadRequest()),
                Arguments.of(new ParkingLotFullException(), status().isBadRequest()),
                Arguments.of(new ParkingLotFullException("For testing .."), status().isBadRequest()),
                Arguments.of(new ParkingLotNotInServiceException(), status().isNotImplemented()),
                Arguments.of(new ParkingLotNotInServiceException("For testing .."), status().isNotImplemented()),
                Arguments.of(new ParkingLotNotFoundException(), status().isNotFound()),
                Arguments.of(new ParkingLotNotFoundException("For testing .."), status().isNotFound())
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsThrownForParkingReservation")
    void parkingSpaceReservationShouldYieldExpected(Exception thrownException, ResultMatcher expected) throws Exception {
        when(this.mockParkingLotManager.reserveParkingAvailability(anyString(), any(VehicleType.class)))
                .thenThrow(thrownException);

        var payload = ParkingReservationRequest.builder()
                .parkingLotId(this.fixture.create(String.class))
                .vehicleType("CAR")
                .build();
        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload)))
                .andExpect(expected);
    }

    @Test
    void parkingLotAdmissionShouldYieldNotFound() throws Exception {
        doThrow(new ReservationNotFoundException())
                .when(this.mockParkingLotManager)
                .admitVehicleForParking(anyString());

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/parking-lots/" + this.fixture.create(String.class)))
                .andExpect(status().isNotFound());
    }

    @Test
    void parkingLotAdmissionShouldYieldAccepted() throws Exception {
        doNothing()
                .when(this.mockParkingLotManager)
                .admitVehicleForParking(anyString());

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/parking-lots/" + this.fixture.create(String.class)))
                .andExpect(status().isAccepted());
    }

    private ParkingLotSummary buildMockSummary(
            int numberOfCars,
            double carRate,
            int numberOfTrucks,
            double truckRate) {
        var rates = Map.of(
                VehicleType.CAR, carRate,
                VehicleType.MONSTERTRUCK, truckRate
        );

        var counts = Map.of(
                VehicleType.CAR, numberOfCars,
                VehicleType.MONSTERTRUCK, numberOfTrucks
        );

        return ParkingLotSummary.builder()
                .parkedVehicleRates(rates)
                .parkedVehicleCounts(counts)
                .build();
    }

    String asJsonString(Object toBeStringified) throws JsonProcessingException {
        return this.mapper.writeValueAsString(toBeStringified);
    }
}
