package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InsightsCalcTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 13);

    private static Map<LocalDate, InsightsCalc.DayStat> days(Object... a) {
        Map<LocalDate, InsightsCalc.DayStat> map = new HashMap<>();
        for (int i = 0; i < a.length; i += 3) {
            map.put(TODAY.minusDays((Integer) a[i]),
                    new InsightsCalc.DayStat((Integer) a[i + 1], (Boolean) a[i + 2]));
        }
        return map;
    }

    @Test
    void weeklyCountsGoalDaysAndComparesWithLastWeek() {
        Map<LocalDate, InsightsCalc.DayStat> d =
                days(0, 100, true, 1, 110, true, 2, 150, false, 3, 90, true, 4, 80, true, 8, 200, true, 9, 180, false);
        InsightsCalc.Weekly w = InsightsCalc.weekly(d, 180, TODAY);
        assertEquals(4, w.getDaysMet());
        assertEquals(5, w.getDaysChecked());
        assertEquals(106, w.getAvgThisWeek());
        assertEquals(190, w.getAvgLastWeek());
        assertTrue(w.isHasLastWeek());
        assertEquals(370, w.getReclaimedMinutes());
    }

    @Test
    void weeklyWithNoCheckInsGivesFriendlyMessage() {
        InsightsCalc.Weekly w = InsightsCalc.weekly(new HashMap<>(), 0, TODAY);
        assertEquals(0, w.getDaysChecked());
        assertFalse(w.isHasGoal());
        assertTrue(w.getLines().get(0).contains("no check-ins"));
    }

    @Test
    void coachSaysHowFarFromNextBadge() {
        List<String> msgs = InsightsCalc.coach(days(0, 100, true, 1, 90, true), true, true, 2, 2, TODAY);
        assertTrue(msgs.get(0).contains("1 day away from the 3-Day Streak badge"));
    }

    @Test
    void coachNotesMissedYesterday() {
        List<String> msgs = InsightsCalc.coach(days(2, 100, true), true, true, 0, 1, TODAY);
        assertTrue(msgs.get(0).startsWith("You missed yesterday"));
    }

    @Test
    void coachAsksNewUserToJoinChallenge() {
        List<String> msgs = InsightsCalc.coach(new HashMap<>(), false, false, 0, 0, TODAY);
        assertEquals(List.of("Join a challenge to start building a streak."), msgs);
    }

    @Test
    void moodReportShowsLowerScreenTimeOnGoodDays() {
        List<InsightsCalc.MoodEntry> entries = List.of(
                new InsightsCalc.MoodEntry(TODAY, "Great"),
                new InsightsCalc.MoodEntry(TODAY.minusDays(1), "Good"),
                new InsightsCalc.MoodEntry(TODAY.minusDays(2), "Tough"));
        InsightsCalc.MoodReport r = InsightsCalc.mood(entries,
                days(0, 100, true, 1, 110, true, 2, 150, false));
        assertTrue(r.isHasData());
        assertTrue(r.getInsight().contains("feel better on lower screen time days"));
        assertEquals(100, r.getRows().get(3).getPercent()); // Tough has the highest average
    }

    @Test
    void moodReportIgnoresReflectionsWithoutCheckIn() {
        InsightsCalc.MoodReport r = InsightsCalc.mood(
                List.of(new InsightsCalc.MoodEntry(TODAY, "Great")), new HashMap<>());
        assertFalse(r.isHasData());
    }

    @Test
    void heatmapCoversWholeWeeksStartingOnMonday() {
        InsightsCalc.Heatmap h = InsightsCalc.heatmap(days(0, 100, true, 1, 150, false), TODAY, 12);
        assertEquals(84, h.getCells().size());
        assertEquals(DayOfWeek.MONDAY, h.getCells().get(0).getDate().getDayOfWeek());
        assertEquals("future", h.getCells().get(83).getState());
        assertEquals(1, h.getMetDays());
        assertEquals(1, h.getMissedDays());
    }
}
