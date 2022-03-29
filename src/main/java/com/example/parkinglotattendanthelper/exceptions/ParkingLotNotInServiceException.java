package com.example.parkinglotattendanthelper.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class ParkingLotNotInServiceException extends RuntimeException{
    public ParkingLotNotInServiceException() {
    }

    public ParkingLotNotInServiceException(String message) {
        super(message);
    }
}
