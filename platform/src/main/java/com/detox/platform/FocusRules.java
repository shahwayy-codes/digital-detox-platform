package com.detox.platform;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Simple rules for the focus timer. No Spring code, so it is easy to unit test. */
public final class FocusRules {

    private FocusRules() {
    }

    public static final List<Integer> OPTIONS = List.of(15, 30, 60);

    /** A session may be completed a few seconds early to allow for clock differences. */
    public static final int GRACE_SECONDS = 3;

    public static boolean validDuration(int minutes) {
        return OPTIONS.contains(minutes);
    }

    public static long remainingSeconds(LocalDateTime startedAt, int plannedMinutes, LocalDateTime now) {
        long total = plannedMinutes * 60L;
        long elapsed = Duration.between(startedAt, now).getSeconds();
        return Math.max(0, total - elapsed);
    }

    public static boolean canComplete(LocalDateTime startedAt, int plannedMinutes, LocalDateTime now) {
        return remainingSeconds(startedAt, plannedMinutes, now) <= GRACE_SECONDS;
    }

    /** 1 point for every 5 focused minutes: 15 min = 3, 30 min = 6, 60 min = 12. */
    public static int points(int completedMinutes) {
        return Math.max(0, completedMinutes) / 5;
    }
}
