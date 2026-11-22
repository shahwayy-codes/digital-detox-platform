package com.detox.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Calculations for the trigger tracker (why did you pick up your phone). No Spring code. */
public final class TriggerCalc {

    private TriggerCalc() {
    }

    public static final List<String> TRIGGERS =
            List.of("Boredom", "Stress", "Notifications", "Habit", "Work or study", "Other");

    private static final Map<String, String> TIPS = new LinkedHashMap<>();
    static {
        TIPS.put("Boredom", "Keep a book or a short offline idea ready for bored moments.");
        TIPS.put("Stress", "Try a two minute breathing break before you reach for your phone.");
        TIPS.put("Notifications", "Switch off non-essential notifications, or try the Notification Cleanse challenge.");
        TIPS.put("Habit", "Put your phone out of reach at the times you usually grab it.");
        TIPS.put("Work or study", "Use the focus timer for work blocks and check messages only in breaks.");
        TIPS.put("Other", "Next time you pick it up, notice the moment and write it in a reflection.");
    }

    /** Returns the matching trigger name, or null when the value is empty or not allowed. */
    public static String clean(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        for (String t : TRIGGERS) {
            if (t.equalsIgnoreCase(v)) {
                return t;
            }
        }
        return null;
    }

    public static class Row {
        private final String name;
        private final int count;
        private final int percent;
        private final int barPercent;

        public Row(String name, int count, int percent, int barPercent) {
            this.name = name;
            this.count = count;
            this.percent = percent;
            this.barPercent = barPercent;
        }

        public String getName() { return name; }
        public int getCount() { return count; }
        public int getPercent() { return percent; }
        public int getBarPercent() { return barPercent; }
    }

    public static class Report {
        private final List<Row> rows;
        private final int total;
        private final String insight;

        public Report(List<Row> rows, int total, String insight) {
            this.rows = rows;
            this.total = total;
            this.insight = insight;
        }

        public List<Row> getRows() { return rows; }
        public int getTotal() { return total; }
        public boolean isHasData() { return total > 0; }
        public String getInsight() { return insight; }
    }

    /** tags: one entry per check-in that has a trigger (null or empty entries are ignored). */
    public static Report breakdown(List<String> tags) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String t : TRIGGERS) {
            counts.put(t, 0);
        }
        int total = 0;
        for (String raw : tags) {
            String t = clean(raw);
            if (t != null) {
                counts.merge(t, 1, Integer::sum);
                total++;
            }
        }
        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Row> rows = new ArrayList<>();
        String top = null;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            int percent = total == 0 ? 0 : e.getValue() * 100 / total;
            int bar = max == 0 ? 0 : e.getValue() * 100 / max;
            rows.add(new Row(e.getKey(), e.getValue(), percent, bar));
            if (top == null || e.getValue() > counts.get(top)) {
                top = e.getKey();
            }
        }
        if (total == 0) {
            return new Report(rows, 0, "Choose a trigger when you check in to see what pulls you to your phone.");
        }
        int topPercent = counts.get(top) * 100 / total;
        String insight = "Your top trigger is " + top + " (" + counts.get(top) + " of " + total
                + " check-ins, " + topPercent + "%). " + TIPS.get(top);
        return new Report(rows, total, insight);
    }
}
