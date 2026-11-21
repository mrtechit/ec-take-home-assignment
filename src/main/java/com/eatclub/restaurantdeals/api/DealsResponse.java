package com.eatclub.restaurantdeals.api;

import java.util.List;

public record DealsResponse(List<ActiveDealResponse> deals) {
}
