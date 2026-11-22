package com.detox.platform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Rule-based challenge suggestions. No AI and no database code. */
public final class RecommendCalc {

    private RecommendCalc() {
    }

    private static final Set<String> HEAVY = Set.of("social media", "screen time", "focus");
    private static final Set<String> BALANCED = Set.of("habits", "mindfulness", "focus");
    private static final Set<String> LIGHT = Set.of("offline", "sleep", "mindfulness");

    public static class Cand {
        private final Long id;
        private final String title;
        private final String category;
        private final int durationDays;

        public Cand(Long id, String title, String category, int durationDays) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.durationDays = durationDays;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public int getDurationDays() { return durationDays; }
    }

    public static class Pick {
        private final Cand challenge;
        private final int score;
        private final String reason;

        public Pick(Cand challenge, int score, String reason) {
            this.challenge = challenge;
            this.score = score;
            this.reason = reason;
        }

        public Long getId() { return challenge.getId(); }
        public String getTitle() { return challenge.getTitle(); }
        public String getCategory() { return challenge.getCategory(); }
        public int getDurationDays() { return challenge.getDurationDays(); }
        public int getScore() { return score; }
        public String getReason() { return reason; }
    }

    /**
     * @param candidates         challenges the user has NOT joined
     * @param joinedCategories   categories of challenges the user already joined
     * @param avgMinutes         average logged screen time per check-in (0 if no check-ins)
     * @param totalCheckIns      how many check-ins the user has done
     * @param max                maximum number of picks
     */
    public static List<Pick> recommend(List<Cand> candidates, Set<String> joinedCategories,
                                       int avgMinutes, int totalCheckIns, int max) {
        Set<String> joined = new java.util.HashSet<>();
        for (String c : joinedCategories) {
            joined.add(c.toLowerCase(Locale.ROOT));
        }
        List<Pick> picks = new ArrayList<>();
        for (Cand c : candidates) {
            String cat = c.getCategory().toLowerCase(Locale.ROOT);
            int score = 0;
            List<String> reasons = new ArrayList<>();

            if (avgMinutes >= 240 && HEAVY.contains(cat)) {
                score += 3;
                reasons.add("You average " + InsightsCalc.duration(avgMinutes)
                        + " a day, so a " + c.getCategory() + " challenge could give you the biggest drop.");
            } else if (avgMinutes >= 120 && avgMinutes < 240 && BALANCED.contains(cat)) {
                score += 2;
                reasons.add("You average " + InsightsCalc.duration(avgMinutes)
                        + " a day, and a " + c.getCategory() + " challenge fits a steady routine.");
            } else if (avgMinutes > 0 && avgMinutes < 120 && LIGHT.contains(cat)) {
                score += 2;
                reasons.add("Your screen time is already low (" + InsightsCalc.duration(avgMinutes)
                        + " a day), so this helps you keep the balance.");
            }

            if (joined.contains(cat)) {
                score += 1;
                reasons.add("You have joined " + c.getCategory() + " challenges before.");
            }

            if (totalCheckIns < 5 && c.getDurationDays() <= 3) {
                score += 2;
                reasons.add("It is only " + c.getDurationDays() + " days long, an easy way to start.");
            }

            if (score > 0) {
                picks.add(new Pick(c, score, String.join(" ", reasons)));
            }
        }
        picks.sort(Comparator.comparingInt(Pick::getScore).reversed()
                .thenComparingInt(Pick::getDurationDays)
                .thenComparing(Pick::getTitle));
        return picks.size() > max ? new ArrayList<>(picks.subList(0, max)) : picks;
    }
}
