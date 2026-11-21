package com.eatclub.restaurantdeals.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eatclub.restaurantdeals.api.ActiveDealResponse;
import com.eatclub.restaurantdeals.api.DealsResponse;
import com.eatclub.restaurantdeals.data.RestaurantDealsData;
import com.eatclub.restaurantdeals.data.RestaurantDealsDataLoader;

@Service
public class ActiveDealsService {

    private final RestaurantDealsData data;

    public ActiveDealsService(RestaurantDealsDataLoader dataLoader) {
        this.data = dataLoader.getData();
    }

    public DealsResponse findActiveDeals(String timeOfDay) {
        LocalTime requestedTime = DealTimeSupport.parseRequestedTime(timeOfDay);
        List<ActiveDealResponse> activeDeals = new ArrayList<>();

        for (RestaurantDealsData.RestaurantDealData restaurant : data.restaurants()) {
            LocalTime restaurantOpen = DealTimeSupport.parseDataTime(
                    restaurant.restaurantOpen(), "restaurantOpen", restaurant.restaurantName());
            LocalTime restaurantClose = DealTimeSupport.parseDataTime(
                    restaurant.restaurantClose(), "restaurantClose", restaurant.restaurantName());

            if (!DealTimeSupport.isWithinHours(requestedTime, restaurantOpen, restaurantClose)
                    || restaurant.deals() == null) {
                continue;
            }

            for (RestaurantDealsData.DealData deal : restaurant.deals()) {
                LocalTime dealStart = deal.dealStart() == null
                        ? restaurantOpen
                        : DealTimeSupport.parseDataTime(
                                deal.dealStart(), "dealStart", restaurant.restaurantName());
                LocalTime dealEnd = deal.dealEnd() == null
                        ? restaurantClose
                        : DealTimeSupport.parseDataTime(
                                deal.dealEnd(), "dealEnd", restaurant.restaurantName());

                if (DealTimeSupport.isWithinHours(requestedTime, dealStart, dealEnd)) {
                    activeDeals.add(toResponse(restaurant, deal));
                }
            }
        }

        return new DealsResponse(List.copyOf(activeDeals));
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
