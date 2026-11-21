package com.eatclub.restaurantdeals.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActiveDealResponse(
        String restaurantObjectId,
        String restaurantName,
        String restaurantAddress1,
        @JsonProperty("restarantSuburb")
        String restaurantSuburb,
        String restaurantOpen,
        String restaurantClose,
        String dealObjectId,
        String discount,
        String dineIn,
        String lightning,
        String qtyLeft) {
}
