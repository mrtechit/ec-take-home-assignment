package com.eatclub.restaurantdeals.service;

public class InvalidTimeOfDayException extends IllegalArgumentException {

    public InvalidTimeOfDayException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTimeOfDayException(String message) {
        super(message);
    }
}
