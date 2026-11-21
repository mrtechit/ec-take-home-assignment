package com.eatclub.restaurantdeals.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.eatclub.restaurantdeals.api.PeakTimeResponse;
import com.eatclub.restaurantdeals.data.RestaurantDealsData;
import com.eatclub.restaurantdeals.data.RestaurantDealsDataLoader;

@Service
public class PeakTimeService {

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final DateTimeFormatter RESPONSE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    private final RestaurantDealsData data;

    public PeakTimeService(RestaurantDealsDataLoader dataLoader) {
        this.data = dataLoader.getData();
    }

    public PeakTimeResponse calculatePeakTime() {
        TreeMap<Integer, Integer> eventDeltas = new TreeMap<>();
        eventDeltas.put(0, 0);
        eventDeltas.put(MINUTES_PER_DAY, 0);

        for (RestaurantDealsData.RestaurantDealData restaurant : data.restaurants()) {
            LocalTime restaurantOpen = DealTimeSupport.parseDataTime(
                    restaurant.restaurantOpen(), "restaurantOpen", restaurant.restaurantName());
            LocalTime restaurantClose = DealTimeSupport.parseDataTime(
                    restaurant.restaurantClose(), "restaurantClose", restaurant.restaurantName());

            if (restaurant.deals() == null) {
                continue;
            }

            List<MinuteInterval> restaurantIntervals = toDailyIntervals(restaurantOpen, restaurantClose);
            for (RestaurantDealsData.DealData deal : restaurant.deals()) {
                LocalTime dealStart = deal.dealStart() == null
                        ? restaurantOpen
                        : DealTimeSupport.parseDataTime(
                                deal.dealStart(), "dealStart", restaurant.restaurantName());
                LocalTime dealEnd = deal.dealEnd() == null
                        ? restaurantClose
                        : DealTimeSupport.parseDataTime(
                                deal.dealEnd(), "dealEnd", restaurant.restaurantName());

                addOverlappingIntervals(
                        eventDeltas,
                        restaurantIntervals,
                        toDailyIntervals(dealStart, dealEnd));
            }
        }

        PeakWindow peak = findPeakWindow(eventDeltas);
        return new PeakTimeResponse(formatTime(peak.startMinute()), formatTime(peak.endMinute()));
    }

    private static List<MinuteInterval> toDailyIntervals(LocalTime open, LocalTime close) {
        int start = open.getHour() * 60 + open.getMinute();
        int end = close.getHour() * 60 + close.getMinute();
        if (start == end) {
            return List.of();
        }
        if (start < end) {
            return List.of(new MinuteInterval(start, end));
        }
        return List.of(
                new MinuteInterval(start, MINUTES_PER_DAY),
                new MinuteInterval(0, end));
    }

    private static void addOverlappingIntervals(
            TreeMap<Integer, Integer> eventDeltas,
            List<MinuteInterval> restaurantIntervals,
            List<MinuteInterval> dealIntervals) {
        for (MinuteInterval restaurantInterval : restaurantIntervals) {
            for (MinuteInterval dealInterval : dealIntervals) {
                int start = Math.max(restaurantInterval.startMinute(), dealInterval.startMinute());
                int end = Math.min(restaurantInterval.endMinute(), dealInterval.endMinute());
                if (start < end) {
                    eventDeltas.merge(start, 1, Integer::sum);
                    eventDeltas.merge(end, -1, Integer::sum);
                }
            }
        }
    }

    private static PeakWindow findPeakWindow(TreeMap<Integer, Integer> eventDeltas) {
        List<Integer> boundaries = new ArrayList<>(eventDeltas.keySet());
        int activeDeals = 0;
        int peakDealCount = 0;
        int peakStart = -1;
        int peakEnd = -1;

        for (int index = 0; index < boundaries.size() - 1; index++) {
            int start = boundaries.get(index);
            int end = boundaries.get(index + 1);
            activeDeals += eventDeltas.get(start);

            if (activeDeals == 0) {
                continue;
            }
            if (activeDeals > peakDealCount) {
                peakDealCount = activeDeals;
                peakStart = start;
                peakEnd = end;
            } else if (activeDeals == peakDealCount && start == peakEnd) {
                peakEnd = end;
            }
        }

        if (peakStart < 0) {
            throw new IllegalStateException("Cannot calculate peak time without active deals");
        }
        return new PeakWindow(peakStart, peakEnd);
    }

    private static String formatTime(int minuteOfDay) {
        int normalizedMinuteOfDay = minuteOfDay % MINUTES_PER_DAY;
        return LocalTime.of(normalizedMinuteOfDay / 60, normalizedMinuteOfDay % 60)
                .format(RESPONSE_TIME_FORMAT);
    }

    private record MinuteInterval(int startMinute, int endMinute) {
    }

    private record PeakWindow(int startMinute, int endMinute) {
    }
}
