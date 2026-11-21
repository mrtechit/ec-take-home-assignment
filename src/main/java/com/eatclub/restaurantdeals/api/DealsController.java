package com.eatclub.restaurantdeals.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatclub.restaurantdeals.service.ActiveDealsService;

@RestController
@RequestMapping("/api/deals")
public class DealsController {

    private final ActiveDealsService activeDealsService;

    public DealsController(ActiveDealsService activeDealsService) {
        this.activeDealsService = activeDealsService;
    }

    @GetMapping
    public DealsResponse getActiveDeals(@RequestParam String timeOfDay) {
        return activeDealsService.findActiveDeals(timeOfDay);
    }
}
