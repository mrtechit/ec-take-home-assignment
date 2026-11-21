package com.eatclub.restaurantdeals.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DealsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsDealsForTwelveHourTime() throws Exception {
        mockMvc.perform(get("/api/deals").param("timeOfDay", "3:00pm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deals").isArray())
                .andExpect(jsonPath("$.deals.length()").value(5))
                .andExpect(jsonPath("$.deals[0].restaurantName").value("Masala Kitchen"))
                .andExpect(jsonPath("$.deals[0].restaurantOpen").value("3:00pm"))
                .andExpect(jsonPath("$.deals[0].restaurantSuburb").value("Lower East"))
                .andExpect(jsonPath("$.deals[0].dealObjectId").value("DEA567C5-0000-3C03-FF00-E3B24909BE00"));
    }

    @Test
    void returnsDealsForTwentyFourHourTime() throws Exception {
        mockMvc.perform(get("/api/deals").param("timeOfDay", "10:30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deals.length()").value(2));
    }

    @Test
    void returnsBadRequestForInvalidTime() throws Exception {
        mockMvc.perform(get("/api/deals").param("timeOfDay", "not-a-time"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid timeOfDay"));
    }

    @Test
    void returnsBadRequestWhenTimeIsMissing() throws Exception {
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isBadRequest());
    }
}
