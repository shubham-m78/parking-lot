package com.example.parking.exception;

public class ParkingException extends RuntimeException {
    private final int status;

    public ParkingException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
