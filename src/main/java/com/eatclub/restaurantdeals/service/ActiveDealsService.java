package com.eatclub.restaurantdeals.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.eatclub.restaurantdeals.api.ActiveDealResponse;
import com.eatclub.restaurantdeals.api.DealsResponse;
import com.eatclub.restaurantdeals.data.RestaurantDealsData;
import com.eatclub.restaurantdeals.data.RestaurantDealsDataLoader;

@Service
public class ActiveDealsService {

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

    private final RestaurantDealsData data;

    public ActiveDealsService(RestaurantDealsDataLoader dataLoader) {
        this.data = dataLoader.getData();
    }

    public DealsResponse findActiveDeals(String timeOfDay) {
        LocalTime requestedTime = parseRequestedTime(timeOfDay);
        List<ActiveDealResponse> activeDeals = new ArrayList<>();

        for (RestaurantDealsData.RestaurantDealData restaurant : data.restaurants()) {
            LocalTime restaurantOpen = parseDataTime(
                    restaurant.restaurantOpen(), "restaurantOpen", restaurant.restaurantName());
            LocalTime restaurantClose = parseDataTime(
                    restaurant.restaurantClose(), "restaurantClose", restaurant.restaurantName());

            if (!isWithinHours(requestedTime, restaurantOpen, restaurantClose) || restaurant.deals() == null) {
                continue;
            }

            for (RestaurantDealsData.DealData deal : restaurant.deals()) {
                LocalTime dealStart = deal.dealStart() == null
                        ? restaurantOpen
                        : parseDataTime(deal.dealStart(), "dealStart", restaurant.restaurantName());
                LocalTime dealEnd = deal.dealEnd() == null
                        ? restaurantClose
                        : parseDataTime(deal.dealEnd(), "dealEnd", restaurant.restaurantName());

                if (isWithinHours(requestedTime, dealStart, dealEnd)) {
                    activeDeals.add(toResponse(restaurant, deal));
                }
            }
        }

        return new DealsResponse(List.copyOf(activeDeals));
    }

    private static LocalTime parseRequestedTime(String value) {
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

    private static LocalTime parseDataTime(String value, String fieldName, String restaurantName) {
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

    private static boolean isWithinHours(LocalTime time, LocalTime open, LocalTime close) {
        if (open.equals(close)) {
            return false;
        }
        if (open.isBefore(close)) {
            return !time.isBefore(open) && time.isBefore(close);
        }
        return !time.isBefore(open) || time.isBefore(close);
    }

    private static ActiveDealResponse toResponse(
            RestaurantDealsData.RestaurantDealData restaurant,
            RestaurantDealsData.DealData deal) {
        return new ActiveDealResponse(
                restaurant.restaurantObjectId(),
                restaurant.restaurantName(),
                restaurant.restaurantAddress1(),
                restaurant.restaurantSuburb(),
                restaurant.restaurantOpen(),
                restaurant.restaurantClose(),
                deal.dealObjectId(),
                deal.discount(),
                deal.dineIn(),
                deal.lightning(),
                deal.qtyLeft());
    }
}
