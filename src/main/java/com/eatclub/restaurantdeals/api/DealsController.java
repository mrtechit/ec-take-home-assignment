package com.eatclub.restaurantdeals.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatclub.restaurantdeals.service.ActiveDealsService;
import com.eatclub.restaurantdeals.service.PeakTimeService;

@RestController
@RequestMapping("/api/deals")
public class DealsController {

    private final ActiveDealsService activeDealsService;
    private final PeakTimeService peakTimeService;

    public DealsController(ActiveDealsService activeDealsService, PeakTimeService peakTimeService) {
        this.activeDealsService = activeDealsService;
        this.peakTimeService = peakTimeService;
    }

    @GetMapping
    public DealsResponse getActiveDeals(@RequestParam String timeOfDay) {
        return activeDealsService.findActiveDeals(timeOfDay);
    }

    @GetMapping("/peak-time")
    public PeakTimeResponse getPeakTime() {
        return peakTimeService.calculatePeakTime();
    }
}
