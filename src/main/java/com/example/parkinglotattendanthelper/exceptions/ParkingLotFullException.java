package com.example.parkinglotattendanthelper.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParkingLotFullException extends RuntimeException{
    public ParkingLotFullException() {
    }

    public ParkingLotFullException(String message) {
        super(message);
    }
}
