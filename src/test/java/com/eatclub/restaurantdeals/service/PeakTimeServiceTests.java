package com.eatclub.restaurantdeals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eatclub.restaurantdeals.api.PeakTimeResponse;

@SpringBootTest
class PeakTimeServiceTests {

    @Autowired
    private PeakTimeService peakTimeService;

    @Test
    void returnsTheEarliestWindowWithTheMostActiveDeals() {
        PeakTimeResponse response = peakTimeService.calculatePeakTime();

        assertEquals("18:00", response.peakTimeStart());
        assertEquals("21:00", response.peakTimeEnd());
    }
}
