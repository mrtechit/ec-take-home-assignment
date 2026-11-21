package com.eatclub.restaurantdeals.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public PeakTimeService(RestaurantDealsDataLoader dataLoader) {
        this(dataLoader.getData());
    }

    PeakTimeService(RestaurantDealsData data) {
        this.data = data;
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

        for (int index = 0; index < boundaries.size() - 1; index++) {
            activeDeals += eventDeltas.get(boundaries.get(index));
            peakDealCount = Math.max(peakDealCount, activeDeals);
        }

        if (peakDealCount == 0) {
            throw new IllegalStateException("Cannot calculate peak time without active deals");
        }

        List<PeakWindow> peakWindows = new ArrayList<>();
        activeDeals = 0;
        int peakStart = -1;

        for (int index = 0; index < boundaries.size() - 1; index++) {
            int start = boundaries.get(index);
            activeDeals += eventDeltas.get(start);

            if (activeDeals == peakDealCount) {
                if (peakStart < 0) {
                    peakStart = start;
                }
            } else if (peakStart >= 0) {
                peakWindows.add(new PeakWindow(peakStart, start));
                peakStart = -1;
            }
        }

        if (peakStart >= 0) {
            peakWindows.add(new PeakWindow(peakStart, boundaries.get(boundaries.size() - 1)));
        }

        if (peakWindows.size() > 1
                && peakWindows.get(0).startMinute() == 0
                && peakWindows.get(peakWindows.size() - 1).endMinute() == MINUTES_PER_DAY) {
            PeakWindow firstWindow = peakWindows.remove(0);
            PeakWindow lastWindow = peakWindows.remove(peakWindows.size() - 1);
            peakWindows.add(new PeakWindow(lastWindow.startMinute(), MINUTES_PER_DAY + firstWindow.endMinute()));
        }

        return peakWindows.stream()
                .min((first, second) -> Integer.compare(first.startMinute(), second.startMinute()))
                .orElseThrow(() -> new IllegalStateException("Cannot calculate peak time without active deals"));
    }

    private static String formatTime(int minuteOfDay) {
        int normalizedMinuteOfDay = Math.floorMod(minuteOfDay, MINUTES_PER_DAY);
        return LocalTime.of(normalizedMinuteOfDay / 60, normalizedMinuteOfDay % 60)
                .format(RESPONSE_TIME_FORMAT);
    }

    private record MinuteInterval(int startMinute, int endMinute) {
    }

    private record PeakWindow(int startMinute, int endMinute) {
    }
}
