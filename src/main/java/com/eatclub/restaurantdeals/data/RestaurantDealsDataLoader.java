package com.eatclub.restaurantdeals.data;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

@Component
public class RestaurantDealsDataLoader {

    private static final String RESOURCE_PATH = "challengedata.json";

    private final RestaurantDealsData data;

    public RestaurantDealsDataLoader(JsonMapper jsonMapper) {
        try (InputStream input = new ClassPathResource(RESOURCE_PATH).getInputStream()) {
            this.data = jsonMapper.readValue(input, RestaurantDealsData.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load restaurant deals from " + RESOURCE_PATH, exception);
        }
    }

    public RestaurantDealsData getData() {
        return data;
    }
}
