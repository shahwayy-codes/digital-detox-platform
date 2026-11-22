package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyticsCalcTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    @Test
    void countsDistinctUsersAndAveragesMinutes() {
        List<AnalyticsCalc.Entry> entries = List.of(
                new AnalyticsCalc.Entry(TODAY, 1, 100),
                new AnalyticsCalc.Entry(TODAY, 1, 200),
                new AnalyticsCalc.Entry(TODAY, 2, 60),
                new AnalyticsCalc.Entry(TODAY.minusDays(1), 3, 90));
        List<AnalyticsCalc.DayPoint> p = AnalyticsCalc.daily(entries, TODAY, 14);
        assertEquals(14, p.size());
        AnalyticsCalc.DayPoint last = p.get(13);
        assertEquals(2, last.getActiveUsers());
        assertEquals(120, last.getAvgMinutes()); // (100 + 200 + 60) / 3
        assertEquals(100, last.getUsersPercent());
        assertEquals(1, p.get(12).getActiveUsers());
        assertEquals(50, p.get(12).getUsersPercent());
        assertEquals(0, p.get(0).getActiveUsers());
    }

    @Test
    void emptyDataGivesZeros() {
        List<AnalyticsCalc.DayPoint> p = AnalyticsCalc.daily(List.of(), TODAY, 7);
        assertEquals(7, p.size());
        assertEquals(0, p.get(6).getUsersPercent());
        assertEquals(0, p.get(6).getMinutesPercent());
    }

    @Test
    void entriesOutsideTheWindowAreIgnored() {
        List<AnalyticsCalc.Entry> entries = List.of(new AnalyticsCalc.Entry(TODAY.minusDays(30), 1, 50));
        List<AnalyticsCalc.DayPoint> p = AnalyticsCalc.daily(entries, TODAY, 14);
        assertEquals(0, p.stream().mapToInt(AnalyticsCalc.DayPoint::getActiveUsers).sum());
    }
}
