package com.detox.platform;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Shared weekly goal for a friend group. No Spring code. */
public final class GroupGoalCalc {

    private GroupGoalCalc() {
    }

    /** Each member is asked for 5 goal days in the last 7 days. */
    public static final int GOAL_DAYS_PER_MEMBER = 5;

    public static class Result {
        private final int members;
        private final int targetDays;
        private final int doneDays;
        private final int percent;
        private final int reclaimedMinutes;

        public Result(int members, int targetDays, int doneDays, int percent, int reclaimedMinutes) {
            this.members = members;
            this.targetDays = targetDays;
            this.doneDays = doneDays;
            this.percent = percent;
            this.reclaimedMinutes = reclaimedMinutes;
        }

        public int getMembers() { return members; }
        public int getTargetDays() { return targetDays; }
        public int getDoneDays() { return doneDays; }
        public int getPercent() { return percent; }
        public boolean isReached() { return members > 0 && doneDays >= targetDays; }
        public int getReclaimedMinutes() { return reclaimedMinutes; }
        public String getReclaimedText() { return InsightsCalc.duration(reclaimedMinutes); }
        public int getRemainingDays() { return Math.max(0, targetDays - doneDays); }
    }

    /**
     * @param memberDays day stats of each member (see InsightsCalc.DayStat)
     * @param goals      each member's daily screen time goal in minutes (0 when not set), same order
     */
    public static Result compute(List<Map<LocalDate, InsightsCalc.DayStat>> memberDays,
                                 List<Integer> goals, LocalDate today) {
        int members = memberDays.size();
        int done = 0;
        int reclaimed = 0;
        for (int m = 0; m < members; m++) {
            int goal = goals.get(m) == null ? 0 : goals.get(m);
            for (int i = 0; i < 7; i++) {
                InsightsCalc.DayStat d = memberDays.get(m).get(today.minusDays(i));
                if (d == null) {
                    continue;
                }
                if (d.isAllMet()) {
                    done++;
                }
                if (goal > 0) {
                    reclaimed += Math.max(0, goal - d.getAvgMinutes());
                }
            }
        }
        int target = members * GOAL_DAYS_PER_MEMBER;
        int percent = target == 0 ? 0 : Math.min(100, done * 100 / target);
        return new Result(members, target, done, percent, reclaimed);
    }
}
