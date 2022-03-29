package com.example.parkinglotattendanthelper.controllers.v1;

import com.example.parkinglotattendanthelper.ParkingLotAttendantHelperApplication;
import com.example.parkinglotattendanthelper.core.IProvideParkingLotData;
import com.example.parkinglotattendanthelper.fixtures.BasicParkingLotDataProvider;
import com.example.parkinglotattendanthelper.fixtures.Common;
import com.example.parkinglotattendanthelper.models.api.ParkingLotSummary;
import com.example.parkinglotattendanthelper.models.api.ParkingReservationRequest;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {ParkingLotAttendantHelperApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ParkingLotControllerComponentTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    IProvideParkingLotData parkingLotDataProvider;

    private WireMockServer wireMockServer;
    private static int mockServerPort = 7878;

    private MockMvc mockMvc;

    private final JFixture fixture = new JFixture();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    void init() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print())
                .build();

        this.wireMockServer = new WireMockServer(WireMockConfiguration.options().port(mockServerPort));
        this.wireMockServer.start();
    }

    @AfterEach
    void reset(){
        this.parkingLotDataProvider.resetParkingLots();
    }

    @AfterAll
    void teardown() {
        this.wireMockServer.stop();
    }

    @Test
    void summaryRetrieveShouldRespondWithGeneratedSummary() throws Exception {
        var response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/parking-lots/" + Common.PARKING_LOT_ONE))
                .andExpect(status().isOk())
                .andReturn();

        var deserialized = this.mapper.readValue(
                response.getResponse().getContentAsString(),
                ParkingLotSummary.class);

        assertEquals(3, deserialized.getParkedVehicleCounts().get(VehicleType.MONSTERTRUCK));
        assertEquals(8, deserialized.getParkedVehicleCounts().get(VehicleType.CAR));
        assertEquals(85d, deserialized.getTotalRevenue());
    }

    @Test
    void summaryRetrieveForUnknownParkingLotShouldThrowException() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/parking-lots/" + this.fixture.create(String.class)))
                .andExpect(status().isNotFound());
    }

    @Test
    void parkingSpaceReservationShouldYieldCreated() throws Exception {
        var payload = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_ONE)
                .vehicleType("CAR")
                .build();

        var response = this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        assertTrue(response.getResponse().getHeader("Location").startsWith("/api/v1/parking-lots"));
    }

    @Test
    void parkingLotPendingReservationsShouldNotAcceptLastReservation() throws Exception {
        var payload1 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("CAR")
                .build();

        var payload2 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("MONSTERTRUCK")
                .build();

        var payload3 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("MONSTERTRUCK")
                .build();

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload1)))
                .andExpect(status().isCreated());

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload2)))
                .andExpect(status().isCreated());

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void parkingSpaceReservationForNonexistantLotShouldYieldBadRequest() throws Exception {
        var payload = ParkingReservationRequest.builder()
                .parkingLotId("PARKING_LOT_ONE")
                .vehicleType("CAR")
                .build();

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void parkingSpaceReservationForUnsupportedVehicleShouldYieldBadRequest() throws Exception {
        var payload = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_ONE)
                .vehicleType("HORSE")
                .build();

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void parkingLotAdmissionShouldYieldNotFound() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/parking-lots/" + this.fixture.create(String.class)))
                .andExpect(status().isNotFound());
    }

    @Test
    void parkingLotAdmissionShouldYieldAccepted() throws Exception {
        var payload1 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("CAR")
                .build();

        var payload2 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("MONSTERTRUCK")
                .build();

        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload1)))
                .andExpect(status().isCreated());

        var response = this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload2)))
                .andExpect(status().isCreated())
                .andReturn();

        var location = response.getResponse().getHeader("Location");

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch(location))
                .andExpect(status().isAccepted());
    }

    @Test
    void admittedVehiclesShouldReflectInReport() throws Exception {
        var payload1 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("CAR")
                .build();

        var payload2 = ParkingReservationRequest.builder()
                .parkingLotId(Common.PARKING_LOT_TINY)
                .vehicleType("MONSTERTRUCK")
                .build();

        this.reserveAndAdmit(payload1);
        this.reserveAndAdmit(payload2);

        // reserve another space .. should not reflect in report
        this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload1)))
                .andExpect(status().isCreated());

        var response = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/parking-lots/" + Common.PARKING_LOT_TINY))
                .andExpect(status().isOk())
                .andReturn();

        var deserialized = this.mapper.readValue(
                response.getResponse().getContentAsString(),
                ParkingLotSummary.class);

        assertEquals(1, deserialized.getParkedVehicleCounts().get(VehicleType.MONSTERTRUCK));
        assertEquals(1, deserialized.getParkedVehicleCounts().get(VehicleType.CAR));
        assertEquals(23d, deserialized.getTotalRevenue());
    }

    void reserveAndAdmit(ParkingReservationRequest payload) throws Exception {
        var response = this.mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/api/v1/parking-lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.asJsonString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        var location = response.getResponse().getHeader("Location");

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch(location))
                .andExpect(status().isAccepted());
    }

    String asJsonString(Object toBeStringified) throws JsonProcessingException {
        return this.mapper.writeValueAsString(toBeStringified);
    }
}
