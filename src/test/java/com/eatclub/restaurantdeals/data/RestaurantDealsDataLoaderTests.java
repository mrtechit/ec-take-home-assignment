package com.eatclub.restaurantdeals.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RestaurantDealsDataLoaderTests {

    @Autowired
    private RestaurantDealsDataLoader dataLoader;

    @Test
    void loadsRestaurantsAndDealsFromClasspathResource() {
        RestaurantDealsData data = dataLoader.getData();

        assertEquals(6, data.restaurants().size());

        RestaurantDealsData.RestaurantDealData kekou = data.restaurants().get(3);
        assertEquals("Kekou", kekou.restaurantName());
        assertEquals("2:00pm", kekou.deals().get(0).dealStart());
        assertEquals("9:00pm", kekou.deals().get(0).dealEnd());

        RestaurantDealsData.DealData dealWithoutTimes = data.restaurants()
                .get(1)
                .deals()
                .get(0);
        assertNull(dealWithoutTimes.dealStart());
        assertNull(dealWithoutTimes.dealEnd());
    }
}
