package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GroupGoalCalcTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private static Map<LocalDate, InsightsCalc.DayStat> member(int metDays, int minutes) {
        Map<LocalDate, InsightsCalc.DayStat> map = new HashMap<>();
        for (int i = 0; i < metDays; i++) {
            map.put(TODAY.minusDays(i), new InsightsCalc.DayStat(minutes, true));
        }
        return map;
    }

    @Test
    void sumsGoalDaysAndReclaimedTime() {
        GroupGoalCalc.Result r = GroupGoalCalc.compute(
                List.of(member(3, 100), member(2, 150)), List.of(180, 180), TODAY);
        assertEquals(2, r.getMembers());
        assertEquals(10, r.getTargetDays());
        assertEquals(5, r.getDoneDays());
        assertEquals(50, r.getPercent());
        assertFalse(r.isReached());
        assertEquals(5, r.getRemainingDays());
        assertEquals(3 * 80 + 2 * 30, r.getReclaimedMinutes());
    }

    @Test
    void goalIsReachedWhenTargetMet() {
        GroupGoalCalc.Result r = GroupGoalCalc.compute(
                List.of(member(5, 100), member(6, 100)), List.of(0, 0), TODAY);
        assertTrue(r.isReached());
        assertEquals(100, r.getPercent());
        assertEquals(0, r.getRemainingDays());
        assertEquals(0, r.getReclaimedMinutes());
    }

    @Test
    void daysOlderThanSevenAreIgnored() {
        Map<LocalDate, InsightsCalc.DayStat> old = new HashMap<>();
        old.put(TODAY.minusDays(10), new InsightsCalc.DayStat(60, true));
        GroupGoalCalc.Result r = GroupGoalCalc.compute(List.of(old), List.of(180), TODAY);
        assertEquals(0, r.getDoneDays());
    }

    @Test
    void emptyGroupIsNeverReached() {
        GroupGoalCalc.Result r = GroupGoalCalc.compute(List.of(), List.of(), TODAY);
        assertFalse(r.isReached());
        assertEquals(0, r.getPercent());
    }
}
