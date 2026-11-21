package com.eatclub.restaurantdeals.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class RestaurantDealsDataDeserializationTests {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void mapsSourceTimeFieldVariantsToCanonicalNames() throws Exception {
        String json = """
                {
                  "restaurants": [{
                    "objectId": "restaurant-1",
                    "name": "Sample Restaurant",
                    "address1": "1 Main Street",
                    "suburb": "Melbourne",
                    "open": "3:00pm",
                    "close": "9:00pm",
                    "cuisines": ["Asian"],
                    "deals": [
                      {"objectId": "deal-1", "open": "3:00pm", "close": "9:00pm"},
                      {"objectId": "deal-2", "start": "4:00pm", "end": "8:00pm"}
                    ]
                  }]
                }
                """;

        RestaurantDealsData.RestaurantDealData restaurant = jsonMapper
                .readValue(json, RestaurantDealsData.class)
                .restaurants()
                .get(0);

        assertEquals("restaurant-1", restaurant.restaurantObjectId());
        assertEquals("Sample Restaurant", restaurant.restaurantName());
        assertEquals("1 Main Street", restaurant.restaurantAddress1());
        assertEquals("Melbourne", restaurant.restaurantSuburb());
        assertEquals("3:00pm", restaurant.restaurantOpen());
        assertEquals("9:00pm", restaurant.restaurantClose());

        assertEquals("3:00pm", restaurant.deals().get(0).dealStart());
        assertEquals("9:00pm", restaurant.deals().get(0).dealEnd());
        assertEquals("4:00pm", restaurant.deals().get(1).dealStart());
        assertEquals("8:00pm", restaurant.deals().get(1).dealEnd());
    }

    @Test
    void keepsMissingDealTimesNull() throws Exception {
        String json = """
                {
                  "restaurants": [{
                    "objectId": "restaurant-1",
                    "deals": [{"objectId": "deal-without-times"}]
                  }]
                }
                """;

        RestaurantDealsData.DealData deal = jsonMapper
                .readValue(json, RestaurantDealsData.class)
                .restaurants()
                .get(0)
                .deals()
                .get(0);

        assertNull(deal.dealStart());
        assertNull(deal.dealEnd());
    }
}
