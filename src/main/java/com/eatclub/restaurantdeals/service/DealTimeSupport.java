package com.eatclub.restaurantdeals.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

final class DealTimeSupport {

    private static final DateTimeFormatter TWELVE_HOUR_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm")
            .optionalStart()
            .appendLiteral(' ')
            .optionalEnd()
            .appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT)
            .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter TWENTY_FOUR_HOUR_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("H:mm")
            .toFormatter(Locale.ROOT);

    private DealTimeSupport() {
    }

    static LocalTime parseRequestedTime(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidTimeOfDayException("timeOfDay must not be blank");
        }

        String normalizedValue = value.trim();
        try {
            return LocalTime.parse(normalizedValue, TWELVE_HOUR_FORMAT);
        } catch (DateTimeParseException twelveHourException) {
            try {
                return LocalTime.parse(normalizedValue, TWENTY_FOUR_HOUR_FORMAT);
            } catch (DateTimeParseException twentyFourHourException) {
                throw new InvalidTimeOfDayException(
                        "timeOfDay must be in h:mma or H:mm format: " + value,
                        twentyFourHourException);
            }
        }
    }

    static LocalTime parseDataTime(String value, String fieldName, String restaurantName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " is required for restaurant " + restaurantName);
        }

        try {
            return LocalTime.parse(value.trim(), TWELVE_HOUR_FORMAT);
        } catch (DateTimeParseException twelveHourException) {
            try {
                return LocalTime.parse(value.trim(), TWENTY_FOUR_HOUR_FORMAT);
            } catch (DateTimeParseException twentyFourHourException) {
                throw new IllegalStateException(
                        "Invalid " + fieldName + " for restaurant " + restaurantName + ": " + value,
                        twentyFourHourException);
            }
        }
    }

    static boolean isWithinHours(LocalTime time, LocalTime open, LocalTime close) {
        if (open.equals(close)) {
            return false;
        }
        if (open.isBefore(close)) {
            return !time.isBefore(open) && time.isBefore(close);
        }
        return !time.isBefore(open) || time.isBefore(close);
    }
}
