package com.eatclub.restaurantdeals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eatclub.restaurantdeals.api.DealsResponse;

@SpringBootTest
class ActiveDealsServiceTests {

    @Autowired
    private ActiveDealsService activeDealsService;

    @Test
    void returnsDealsAtThreePmOnlyWhenRestaurantIsOpen() {
        DealsResponse response = activeDealsService.findActiveDeals("3:00pm");

        assertEquals(5, response.deals().size(), response.deals().toString());
        assertTrue(response.deals().stream().allMatch(
                deal -> deal.restaurantName().equals("Masala Kitchen")
                        || deal.restaurantName().equals("ABC Chicken")
                        || deal.restaurantName().equals("Kekou")));
        assertTrue(response.deals().stream().anyMatch(
                deal -> deal.restaurantName().equals("ABC Chicken")));
        assertFalse(response.deals().stream().anyMatch(
                deal -> deal.restaurantName().equals("Vrindavan")
                        || deal.restaurantName().equals("OzzyThai Cafe Bar ")));
    }

    @Test
    void returnsNineDealsAtSixPmIncludingDealsWithoutTheirOwnHours() {
        DealsResponse response = activeDealsService.findActiveDeals("6:00pm");

        assertEquals(9, response.deals().size());
        assertTrue(response.deals().stream().anyMatch(
                deal -> deal.restaurantName().equals("ABC Chicken")));
        assertTrue(response.deals().stream().anyMatch(
                deal -> deal.restaurantName().equals("Vrindavan")));
        assertFalse(response.deals().stream().anyMatch(
                deal -> deal.restaurantName().equals("OzzyThai Cafe Bar ")));
    }

    @Test
    void excludesDealsAtTheirClosingTime() {
        DealsResponse response = activeDealsService.findActiveDeals("9:00pm");

        assertEquals(4, response.deals().size());
        assertTrue(response.deals().stream().allMatch(
                deal -> deal.restaurantName().equals("ABC Chicken")
                        || deal.restaurantName().equals("Gyoza Gyoza Melbourne Central")));
    }

    @Test
    void acceptsTwentyFourHourInput() {
        DealsResponse response = activeDealsService.findActiveDeals("10:30");

        assertEquals(2, response.deals().size());
        assertTrue(response.deals().stream().allMatch(
                deal -> deal.restaurantName().equals("OzzyThai Cafe Bar ")));
    }

    @Test
    void rejectsUnparseableTimeOfDay() {
        assertThrows(IllegalArgumentException.class, () -> activeDealsService.findActiveDeals("not-a-time"));
    }
}
