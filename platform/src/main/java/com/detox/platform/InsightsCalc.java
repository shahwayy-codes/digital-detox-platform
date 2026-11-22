package com.detox.platform;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure calculation logic for the insights features (weekly report, streak coach,
 * activity heatmap, mood versus screen time). It has no Spring or database code,
 * so it is easy to unit test. InsightsService feeds it the data.
 */
public final class InsightsCalc {

    private InsightsCalc() {
    }

    public static final List<String> MOODS = List.of("Great", "Good", "Okay", "Tough");

    // ---------- input models ----------

    /** One day of data. avgMinutes is the average of that day's check-ins. */
    public static class DayStat {
        private final int avgMinutes;
        private final boolean allMet;

        public DayStat(int avgMinutes, boolean allMet) {
            this.avgMinutes = avgMinutes;
            this.allMet = allMet;
        }

        public int getAvgMinutes() { return avgMinutes; }
        public boolean isAllMet() { return allMet; }
    }

    public static class MoodEntry {
        private final LocalDate date;
        private final String mood;

        public MoodEntry(LocalDate date, String mood) {
            this.date = date;
            this.mood = mood;
        }

        public LocalDate getDate() { return date; }
        public String getMood() { return mood; }
    }

    // ---------- output models ----------

    public static class Weekly {
        private final int daysChecked;
        private final int daysMet;
        private final int avgThisWeek;
        private final int avgLastWeek;
        private final boolean hasLastWeek;
        private final int reclaimedMinutes;
        private final boolean hasGoal;
        private final List<String> lines;

        public Weekly(int daysChecked, int daysMet, int avgThisWeek, int avgLastWeek, boolean hasLastWeek,
                      int reclaimedMinutes, boolean hasGoal, List<String> lines) {
            this.daysChecked = daysChecked;
            this.daysMet = daysMet;
            this.avgThisWeek = avgThisWeek;
            this.avgLastWeek = avgLastWeek;
            this.hasLastWeek = hasLastWeek;
            this.reclaimedMinutes = reclaimedMinutes;
            this.hasGoal = hasGoal;
            this.lines = lines;
        }

        public int getDaysChecked() { return daysChecked; }
        public int getDaysMet() { return daysMet; }
        public int getAvgThisWeek() { return avgThisWeek; }
        public int getAvgLastWeek() { return avgLastWeek; }
        public boolean isHasLastWeek() { return hasLastWeek; }
        public int getReclaimedMinutes() { return reclaimedMinutes; }
        public boolean isHasGoal() { return hasGoal; }
        public List<String> getLines() { return lines; }
        public String getReclaimedText() { return duration(reclaimedMinutes); }
    }

    public static class MoodRow {
        private final String mood;
        private final int count;
        private final int avgMinutes;
        private final int percent;

        public MoodRow(String mood, int count, int avgMinutes, int percent) {
            this.mood = mood;
            this.count = count;
            this.avgMinutes = avgMinutes;
            this.percent = percent;
        }

        public String getMood() { return mood; }
        public int getCount() { return count; }
        public int getAvgMinutes() { return avgMinutes; }
        public int getPercent() { return percent; }
    }

    public static class MoodReport {
        private final List<MoodRow> rows;
        private final boolean hasData;
        private final String insight;

        public MoodReport(List<MoodRow> rows, boolean hasData, String insight) {
            this.rows = rows;
            this.hasData = hasData;
            this.insight = insight;
        }

        public List<MoodRow> getRows() { return rows; }
        public boolean isHasData() { return hasData; }
        public String getInsight() { return insight; }
    }

    public static class HeatCell {
        private final LocalDate date;
        private final String state; // met, missed, none, future
        private final String tooltip;

        public HeatCell(LocalDate date, String state, String tooltip) {
            this.date = date;
            this.state = state;
            this.tooltip = tooltip;
        }

        public LocalDate getDate() { return date; }
        public String getState() { return state; }
        public String getTooltip() { return tooltip; }
    }

    public static class Heatmap {
        private final List<HeatCell> cells;
        private final int metDays;
        private final int missedDays;
        private final int weeks;

        public Heatmap(List<HeatCell> cells, int metDays, int missedDays, int weeks) {
            this.cells = cells;
            this.metDays = metDays;
            this.missedDays = missedDays;
            this.weeks = weeks;
        }

        public List<HeatCell> getCells() { return cells; }
        public int getMetDays() { return metDays; }
        public int getMissedDays() { return missedDays; }
        public int getWeeks() { return weeks; }
    }

    // ---------- helpers ----------

    /** 380 -> "6 h 20 min", 45 -> "45 min", 120 -> "2 h". */
    public static String duration(int minutes) {
        int m = Math.max(0, minutes);
        int h = m / 60;
        int rest = m % 60;
        if (h == 0) {
            return rest + " min";
        }
        if (rest == 0) {
            return h + " h";
        }
        return h + " h " + rest + " min";
    }

    private static String plural(int n, String word) {
        return n + " " + word + (n == 1 ? "" : "s");
    }

    // ---------- 1. weekly insights ----------

    public static Weekly weekly(Map<LocalDate, DayStat> days, int goalMinutes, LocalDate today) {
        int checked = 0;
        int met = 0;
        int sumThis = 0;
        for (int i = 0; i < 7; i++) {
            DayStat d = days.get(today.minusDays(i));
            if (d != null) {
                checked++;
                sumThis += d.getAvgMinutes();
                if (d.isAllMet()) {
                    met++;
                }
            }
        }
        int checkedLast = 0;
        int sumLast = 0;
        for (int i = 7; i < 14; i++) {
            DayStat d = days.get(today.minusDays(i));
            if (d != null) {
                checkedLast++;
                sumLast += d.getAvgMinutes();
            }
        }
        int avgThis = checked == 0 ? 0 : Math.round((float) sumThis / checked);
        int avgLast = checkedLast == 0 ? 0 : Math.round((float) sumLast / checkedLast);
        boolean hasLast = checked > 0 && checkedLast > 0;

        boolean hasGoal = goalMinutes > 0;
        int reclaimed = 0;
        if (hasGoal) {
            LocalDate d = today.withDayOfMonth(1);
            while (!d.isAfter(today)) {
                DayStat s = days.get(d);
                if (s != null) {
                    reclaimed += Math.max(0, goalMinutes - s.getAvgMinutes());
                }
                d = d.plusDays(1);
            }
        }

        List<String> lines = new ArrayList<>();
        if (checked == 0) {
            lines.add("You have no check-ins in the last 7 days yet. Check in today to start your report.");
        } else {
            lines.add("You met your goal on " + met + " of the last 7 days (" + checked + " checked in).");
            if (hasLast) {
                int diff = avgThis - avgLast;
                if (diff < 0) {
                    lines.add("Your average screen time was " + duration(avgThis) + " a day, "
                            + duration(-diff) + " less per day than the week before.");
                } else if (diff > 0) {
                    lines.add("Your average screen time was " + duration(avgThis) + " a day, "
                            + duration(diff) + " more per day than the week before.");
                } else {
                    lines.add("Your average screen time was " + duration(avgThis)
                            + " a day, the same as the week before.");
                }
            } else {
                lines.add("Your average screen time was " + duration(avgThis)
                        + " a day. Keep checking in to compare with next week.");
            }
        }
        if (hasGoal) {
            lines.add("Compared with your daily goal of " + goalMinutes + " min, you reclaimed "
                    + duration(reclaimed) + " this month.");
        } else {
            lines.add("Set a daily screen time goal in your profile to see how much time you reclaim each month.");
        }
        return new Weekly(checked, met, avgThis, avgLast, hasLast, reclaimed, hasGoal, lines);
    }

    // ---------- 2. streak coach ----------

    public static List<String> coach(Map<LocalDate, DayStat> days, boolean hasChallenge,
                                     boolean joinedBeforeYesterday, int currentStreak,
                                     int longestStreak, LocalDate today) {
        List<String> msgs = new ArrayList<>();
        if (!hasChallenge) {
            msgs.add("Join a challenge to start building a streak.");
            return msgs;
        }
        boolean doneToday = days.containsKey(today);
        DayStat yesterday = days.get(today.minusDays(1));

        if (joinedBeforeYesterday && !doneToday) {
            if (yesterday == null) {
                msgs.add("You missed yesterday, so check in today to restart your streak.");
            } else if (!yesterday.isAllMet()) {
                msgs.add("Yesterday went over your goal. Check in today to get back on track.");
            }
        }

        if (currentStreak > 0) {
            int target = 0;
            if (longestStreak < 3) {
                target = 3;
            } else if (longestStreak < 7) {
                target = 7;
            }
            if (target > 0) {
                int left = target - currentStreak;
                if (left > 0) {
                    msgs.add("You are " + plural(left, "day") + " away from the " + target
                            + "-Day Streak badge.");
                }
            } else {
                msgs.add("Great work, your current streak is " + currentStreak + " days. Keep it going.");
            }
        }

        if (!doneToday && currentStreak > 0) {
            msgs.add("Check in today to keep your streak alive.");
        }
        if (msgs.isEmpty() && doneToday) {
            msgs.add("You are all caught up for today. Nice work.");
        }
        if (msgs.isEmpty()) {
            msgs.add("Check in today and meet your goal to begin a new streak.");
        }
        return msgs.size() > 3 ? new ArrayList<>(msgs.subList(0, 3)) : msgs;
    }

    // ---------- 3. mood versus screen time ----------

    public static MoodReport mood(List<MoodEntry> entries, Map<LocalDate, DayStat> days) {
        int[] count = new int[MOODS.size()];
        int[] sum = new int[MOODS.size()];
        for (MoodEntry e : entries) {
            int idx = MOODS.indexOf(e.getMood());
            DayStat d = days.get(e.getDate());
            if (idx < 0 || d == null) {
                continue;
            }
            count[idx]++;
            sum[idx] += d.getAvgMinutes();
        }
        int total = 0;
        int max = 0;
        int[] avg = new int[MOODS.size()];
        for (int i = 0; i < MOODS.size(); i++) {
            total += count[i];
            avg[i] = count[i] == 0 ? 0 : Math.round((float) sum[i] / count[i]);
            max = Math.max(max, avg[i]);
        }
        List<MoodRow> rows = new ArrayList<>();
        for (int i = 0; i < MOODS.size(); i++) {
            int percent = max == 0 ? 0 : avg[i] * 100 / max;
            rows.add(new MoodRow(MOODS.get(i), count[i], avg[i], percent));
        }
        if (total == 0) {
            return new MoodReport(rows, false,
                    "Write a reflection on a day you checked in to see how your mood relates to your screen time.");
        }

        int goodCount = count[0] + count[1];
        int lowCount = count[2] + count[3];
        String insight;
        if (goodCount == 0 || lowCount == 0) {
            insight = "Reflect on a mix of good and tough days to compare your mood with your screen time.";
        } else {
            int goodAvg = Math.round((float) (sum[0] + sum[1]) / goodCount);
            int lowAvg = Math.round((float) (sum[2] + sum[3]) / lowCount);
            if (goodAvg + 10 <= lowAvg) {
                insight = "You tend to feel better on lower screen time days: about " + duration(goodAvg)
                        + " on Great or Good days versus " + duration(lowAvg) + " on Okay or Tough days.";
            } else if (lowAvg + 10 <= goodAvg) {
                insight = "Your mood does not seem to drop on higher screen time days: Great or Good days averaged "
                        + duration(goodAvg) + " versus " + duration(lowAvg) + " on Okay or Tough days.";
            } else {
                insight = "There is no clear link between your mood and your screen time yet. "
                        + "Keep reflecting to see a pattern.";
            }
        }
        if (total < 4) {
            insight += " This is based on only " + plural(total, "reflection") + ", so treat it as a first look.";
        }
        return new MoodReport(rows, true, insight);
    }

    // ---------- 4. activity heatmap ----------

    /** A grid of weeks (Monday to Sunday). The last column is the current week. */
    public static Heatmap heatmap(Map<LocalDate, DayStat> days, LocalDate today, int weeks) {
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(weeks - 1L);
        LocalDate end = start.plusWeeks(weeks).minusDays(1);
        List<HeatCell> cells = new ArrayList<>();
        int met = 0;
        int missed = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " "
                    + d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (d.isAfter(today)) {
                cells.add(new HeatCell(d, "future", label));
                continue;
            }
            DayStat s = days.get(d);
            if (s == null) {
                cells.add(new HeatCell(d, "none", label + ": no check-in"));
            } else if (s.isAllMet()) {
                met++;
                cells.add(new HeatCell(d, "met", label + ": " + s.getAvgMinutes() + " min, goal met"));
            } else {
                missed++;
                cells.add(new HeatCell(d, "missed", label + ": " + s.getAvgMinutes() + " min, goal missed"));
            }
        }
        return new Heatmap(cells, met, missed, weeks);
    }
}
