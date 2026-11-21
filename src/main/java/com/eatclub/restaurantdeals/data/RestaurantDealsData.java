package com.eatclub.restaurantdeals.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RestaurantDealsData(List<RestaurantDealData> restaurants) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RestaurantDealData(
            @JsonAlias("objectId") String restaurantObjectId,
            @JsonAlias("name") String restaurantName,
            @JsonAlias("address1") String restaurantAddress1,
            @JsonAlias("suburb") String restaurantSuburb,
            @JsonAlias("open") String restaurantOpen,
            @JsonAlias("close") String restaurantClose,
            List<DealData> deals) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DealData(
            @JsonAlias("objectId") String dealObjectId,
            String discount,
            String dineIn,
            String lightning,
            @JsonAlias({"open", "start"}) String dealStart,
            @JsonAlias({"close", "end"}) String dealEnd,
            String qtyLeft) {
    }
}
