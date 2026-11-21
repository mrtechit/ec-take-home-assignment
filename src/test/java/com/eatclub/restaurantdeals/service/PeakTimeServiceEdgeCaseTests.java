package com.eatclub.restaurantdeals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.eatclub.restaurantdeals.api.PeakTimeResponse;
import com.eatclub.restaurantdeals.data.RestaurantDealsData;
import com.eatclub.restaurantdeals.data.RestaurantDealsData.DealData;
import com.eatclub.restaurantdeals.data.RestaurantDealsData.RestaurantDealData;

class PeakTimeServiceEdgeCaseTests {

    @Test
    void joinsPeakIntervalsAcrossMidnight() {
        PeakTimeService service = new PeakTimeService(data(
                restaurant("10:00pm", "2:00am", List.of(deal(null, null)))));

        PeakTimeResponse response = service.calculatePeakTime();

        assertEquals("22:00", response.peakTimeStart());
        assertEquals("02:00", response.peakTimeEnd());
    }

    @Test
    void choosesEarliestWhenPeakWindowsAreTied() {
        PeakTimeService service = new PeakTimeService(data(
                restaurant("8:00am", "10:00pm", List.of(
                        deal("9:00am", "10:00am"),
                        deal("6:00pm", "7:00pm")))));

        PeakTimeResponse response = service.calculatePeakTime();

        assertEquals("09:00", response.peakTimeStart());
        assertEquals("10:00", response.peakTimeEnd());
    }

    @Test
    void failsClearlyWhenThereAreNoActiveDeals() {
        PeakTimeService service = new PeakTimeService(data(
                restaurant("8:00am", "10:00pm", List.of())));

        assertThrows(IllegalStateException.class, service::calculatePeakTime);
    }

    private static RestaurantDealsData data(RestaurantDealData... restaurants) {
        return new RestaurantDealsData(List.of(restaurants));
    }

    private static RestaurantDealData restaurant(
            String open,
            String close,
            List<DealData> deals) {
        return new RestaurantDealData(
                "restaurant-id",
                "Test Restaurant",
                "1 Main Street",
                "Melbourne",
                open,
                close,
                deals);
    }

    private static DealData deal(String start, String end) {
        return new DealData("deal-id", "20", "true", "false", start, end, "1");
    }
}
