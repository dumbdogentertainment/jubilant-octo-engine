package com.example.parkinglotattendanthelper.fixtures;

import com.example.parkinglotattendanthelper.core.IProvideParkingLotData;
import com.example.parkinglotattendanthelper.exceptions.BadRequestException;
import com.example.parkinglotattendanthelper.exceptions.ParkingLotFullException;
import com.example.parkinglotattendanthelper.exceptions.ParkingLotNotFoundException;
import com.example.parkinglotattendanthelper.exceptions.ReservationNotFoundException;
import com.example.parkinglotattendanthelper.models.common.VehicleType;
import com.example.parkinglotattendanthelper.models.service.BasicParkingBlock;
import com.example.parkinglotattendanthelper.models.service.BasicParkingLot;
import com.flextrade.jfixture.JFixture;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BasicParkingLotDataProvider implements IProvideParkingLotData {
    final JFixture fixture = new JFixture();

    Map<String, Map<VehicleType, Double>> parkingRates = new HashMap<>();
    Map<String, BasicParkingLot> parkingLots = new HashMap<>();
    Map<String, Pair<String, String>> parkingReservations = new HashMap<>();


    public BasicParkingLotDataProvider() {
        this.parkingRates.putIfAbsent(Common.PARKING_LOT_ONE, Map.of(VehicleType.CAR, 5d, VehicleType.MONSTERTRUCK, 15d));
        this.parkingLots.putIfAbsent(Common.PARKING_LOT_ONE, buildParkingLotOne());

        this.parkingRates.putIfAbsent(Common.PARKING_LOT_TWO, Map.of(VehicleType.CAR, 8d, VehicleType.MONSTERTRUCK, 25d));
        this.parkingLots.putIfAbsent(Common.PARKING_LOT_TWO, buildParkingLotTwo());

        this.parkingRates.putIfAbsent(Common.PARKING_LOT_TINY, Map.of(VehicleType.CAR, 3d, VehicleType.MONSTERTRUCK, 20d));
        this.parkingLots.putIfAbsent(Common.PARKING_LOT_TINY, buildParkingLotTiny());
    }

    @Override
    public void resetParkingLots() {
        this.parkingLots.replace(Common.PARKING_LOT_ONE, buildParkingLotOne());
        this.parkingLots.replace(Common.PARKING_LOT_TWO, buildParkingLotTwo());
        this.parkingLots.replace(Common.PARKING_LOT_TINY, buildParkingLotTiny());
    }

    @Override
    public BasicParkingLot retrieveParkingLotData(String parkingLotId) {
        if (this.parkingLots.containsKey(parkingLotId)) {
            return this.parkingLots.get(parkingLotId);
        }

        throw new ParkingLotNotFoundException("Parking lot not found.");
    }

    @Override
    public String reserveParkingSpace(String parkingLotId, VehicleType vehicleType) {
        if (!this.parkingLots.containsKey(parkingLotId)) {
            throw new ParkingLotNotFoundException("Parking lot not found.");
        }
        var lot = this.parkingLots.get(parkingLotId);

        var openBlocks = lot.getParkingLotBlocks().values().stream()
                .filter(block -> block.hasSpaceFor(vehicleType))
                .collect(Collectors.toList());

        if (openBlocks.isEmpty()) {
            throw new ParkingLotFullException("No spaces available.");
        }

        var block = openBlocks.stream().findFirst().get();
        var reserve = StringUtils.EMPTY;
        var post = StringUtils.EMPTY;
        switch (vehicleType) {
            case MONSTERTRUCK:
                reserve = block.getParkingSpaces().replaceFirst("__", "!!");
                post = reserve.replaceFirst("!!", "MT");
                break;
            case CAR:
                reserve = block.getParkingSpaces().replaceFirst("_", "!");
                post = reserve.replaceFirst("!", "C");
                break;
        }
        block.setParkingSpaces(reserve);

        var confirmKey = String.format("%s-%s-%s", parkingLotId, block.getParkingBlockId(), vehicleType.getAsString());
        this.parkingReservations.putIfAbsent(confirmKey, Pair.of(reserve, post));

        return confirmKey;
    }

    @Override
    public void admitVehicle(String parkingReservationId) {
        if (!this.parkingReservations.containsKey(parkingReservationId)) {
            throw new ReservationNotFoundException("Parking reservation not found");
        }

        var split = parkingReservationId.split("-");
        if (split.length < 3) {
            // don't tip off that the id provided is invalid
            throw new ReservationNotFoundException("Parking reservation not found");
        }

        var lot = this.parkingLots.get(split[0]);
        var block = lot.getParkingLotBlocks().get(split[1]);

        var reservation = this.parkingReservations.get(parkingReservationId);

        if (block.getParkingSpaces().equalsIgnoreCase(reservation.getLeft())) {
            block.setParkingSpaces(reservation.getRight());
        }
    }

    BasicParkingBlock buildBasicBlock(String id, String spaces) {
        return BasicParkingBlock.builder()
                .parkingBlockId(id)
                .parkingSpaces(spaces)
                .build();
    }

    BasicParkingLot buildParkingLotOne() {
        return BasicParkingLot.builder()
                .parkingLotRates(Map.of(VehicleType.CAR, 5d, VehicleType.MONSTERTRUCK, 15d))
                .parkingLotBlocks(Map.of(
                        "block1", this.buildBasicBlock("block1", "!CCMT!_C!!"),
                        "block2", this.buildBasicBlock("block2", "!_C!!__MT_"),
                        "block3", this.buildBasicBlock("block3", "!!___CC!!!"),
                        "block4", this.buildBasicBlock("block4", "CCMT____!!")
                ))
                .build();
    }

    BasicParkingLot buildParkingLotTwo() {
        return BasicParkingLot.builder()
                .parkingLotRates(Map.of(VehicleType.CAR, 8d, VehicleType.MONSTERTRUCK, 25d))
                .parkingLotBlocks(Map.of(
                        "block1", this.buildBasicBlock("block1", "!CCMT!CC!!"),
                        "block2", this.buildBasicBlock("block2", "!_C!!C_MT_"),
                        "block3", this.buildBasicBlock("block3", "!!___CC!!C"),
                        "block4", this.buildBasicBlock("block4", "!!MTMTMT!!")
                ))
                .build();
    }

    BasicParkingLot buildParkingLotTiny() {
        return BasicParkingLot.builder()
                .parkingLotRates(Map.of(VehicleType.CAR, 3d, VehicleType.MONSTERTRUCK, 20d))
                .parkingLotBlocks(Map.of(
                        "block1", this.buildBasicBlock("block1", "__"),
                        "block2", this.buildBasicBlock("block2", "__")
                ))
                .build();
    }
}
