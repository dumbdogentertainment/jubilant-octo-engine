package com.example.parkinglotattendanthelper.controllers;

//import com.example.parkinglotattendanthelper.exceptions.NotImplementedException;
import com.example.parkinglotattendanthelper.exceptions.*;
import org.apache.commons.lang3.NotImplementedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CustomErrorAdvice {
    private ResponseEntity<String> buildErrorResponse(
            String errorMessage,
            HttpStatus errorCode) {
        return ResponseEntity
                .status(errorCode)
                .body(errorMessage);
    }

    @ExceptionHandler(ParkingLotNotInServiceException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(final ParkingLotNotInServiceException caughtException) {
        return buildErrorResponse(
                StringUtils.isEmpty(caughtException.getMessage()) ?
                        "Parking lot not in service yet .." :
                        caughtException.getMessage(),
                HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(final BadRequestException caughtException) {
        return buildErrorResponse(
                StringUtils.isEmpty(caughtException.getMessage()) ?
                        "Bad request encountered .." :
                        caughtException.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParkingLotFullException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(final ParkingLotFullException caughtException) {
        return buildErrorResponse(
                StringUtils.isEmpty(caughtException.getMessage()) ?
                        "Parking not available at this time (lot full) .." :
                        caughtException.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParkingLotNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(final ParkingLotNotFoundException caughtException) {
        return buildErrorResponse(
                StringUtils.isEmpty(caughtException.getMessage()) ?
                        "Parking lot not found .." :
                        caughtException.getMessage(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(final ReservationNotFoundException caughtException) {
        return buildErrorResponse(
                StringUtils.isEmpty(caughtException.getMessage()) ?
                        "Parking reservation not found .." :
                        caughtException.getMessage(),
                HttpStatus.NOT_FOUND);
    }
}
