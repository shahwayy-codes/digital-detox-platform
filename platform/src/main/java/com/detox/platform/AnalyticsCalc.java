package com.detox.platform;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Platform-wide numbers for the admin analytics page. No Spring code. */
public final class AnalyticsCalc {

    private AnalyticsCalc() {
    }

    public static class Entry {
        private final LocalDate date;
        private final long userId;
        private final int minutes;

        public Entry(LocalDate date, long userId, int minutes) {
            this.date = date;
            this.userId = userId;
            this.minutes = minutes;
        }

        public LocalDate getDate() { return date; }
        public long getUserId() { return userId; }
        public int getMinutes() { return minutes; }
    }

    public static class DayPoint {
        private final String label;
        private final int activeUsers;
        private final int avgMinutes;
        private final int usersPercent;
        private final int minutesPercent;

        public DayPoint(String label, int activeUsers, int avgMinutes, int usersPercent, int minutesPercent) {
            this.label = label;
            this.activeUsers = activeUsers;
            this.avgMinutes = avgMinutes;
            this.usersPercent = usersPercent;
            this.minutesPercent = minutesPercent;
        }

        public String getLabel() { return label; }
        public int getActiveUsers() { return activeUsers; }
        public int getAvgMinutes() { return avgMinutes; }
        public int getUsersPercent() { return usersPercent; }
        public int getMinutesPercent() { return minutesPercent; }
    }

    /** The last `days` days ending today, oldest first. */
    public static List<DayPoint> daily(List<Entry> entries, LocalDate today, int days) {
        int[] users = new int[days];
        int[] avg = new int[days];
        for (int i = 0; i < days; i++) {
            LocalDate d = today.minusDays(days - 1L - i);
            Set<Long> ids = new HashSet<>();
            int sum = 0;
            int n = 0;
            for (Entry e : entries) {
                if (e.getDate().equals(d)) {
                    ids.add(e.getUserId());
                    sum += e.getMinutes();
                    n++;
                }
            }
            users[i] = ids.size();
            avg[i] = n == 0 ? 0 : Math.round((float) sum / n);
        }
        int maxUsers = 0;
        int maxAvg = 0;
        for (int i = 0; i < days; i++) {
            maxUsers = Math.max(maxUsers, users[i]);
            maxAvg = Math.max(maxAvg, avg[i]);
        }
        List<DayPoint> points = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = today.minusDays(days - 1L - i);
            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + d.getDayOfMonth();
            points.add(new DayPoint(label, users[i], avg[i],
                    maxUsers == 0 ? 0 : users[i] * 100 / maxUsers,
                    maxAvg == 0 ? 0 : avg[i] * 100 / maxAvg));
        }
        return points;
    }
}
