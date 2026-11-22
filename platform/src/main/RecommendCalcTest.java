package com.detox.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecommendCalcTest {

    private static final List<RecommendCalc.Cand> ALL = List.of(
            new RecommendCalc.Cand(1L, "No Social Media Weekend", "Social media", 2),
            new RecommendCalc.Cand(2L, "Phone-Free Mornings", "Habits", 7),
            new RecommendCalc.Cand(3L, "Offline Hobby Week", "Offline", 7),
            new RecommendCalc.Cand(4L, "Notification Cleanse", "Focus", 3),
            new RecommendCalc.Cand(5L, "Digital Sunset", "Sleep", 14));

    @Test
    void heavyUserGetsCutBackChallengesFirst() {
        List<RecommendCalc.Pick> picks = RecommendCalc.recommend(ALL, Set.of(), 300, 20, 3);
        assertEquals(3, picks.get(0).getScore());
        assertTrue(picks.get(0).getReason().contains("5 h"));
        assertEquals("No Social Media Weekend", picks.get(0).getTitle()); // same score, shorter duration first
        assertEquals("Notification Cleanse", picks.get(1).getTitle());
        assertEquals(2, picks.size());
    }

    @Test
    void lightUserGetsOfflineAndSleep() {
        List<RecommendCalc.Pick> picks = RecommendCalc.recommend(ALL, Set.of(), 90, 20, 3);
        assertEquals(2, picks.size());
        assertEquals("Offline Hobby Week", picks.get(0).getTitle());
        assertEquals("Digital Sunset", picks.get(1).getTitle());
    }

    @Test
    void newUserGetsShortChallenges() {
        List<RecommendCalc.Pick> picks = RecommendCalc.recommend(ALL, Set.of(), 0, 0, 3);
        assertEquals(2, picks.size());
        assertEquals("No Social Media Weekend", picks.get(0).getTitle());
        assertTrue(picks.get(0).getReason().contains("easy way to start"));
    }

    @Test
    void pastCategoriesAddBonus() {
        List<RecommendCalc.Pick> picks = RecommendCalc.recommend(ALL, Set.of("habits"), 150, 20, 3);
        assertEquals("Phone-Free Mornings", picks.get(0).getTitle());
        assertEquals(3, picks.get(0).getScore()); // balanced (2) + past category (1)
    }

    @Test
    void respectsMaximum() {
        assertEquals(1, RecommendCalc.recommend(ALL, Set.of(), 300, 20, 1).size());
    }

    @Test
    void balancedUserDoesNotGetOfflineChallenge() {
        assertTrue(RecommendCalc.recommend(ALL, Set.of(), 150, 20, 3).stream()
                .noneMatch(p -> p.getTitle().equals("Offline Hobby Week")));
    }
}
