package com.detox.platform;

import java.util.ArrayList;
import java.util.List;

/** Daily quests shown on the dashboard and in the side panel. No Spring code. */
public final class QuestCalc {

    private QuestCalc() {
    }

    public static final int FOCUS_TARGET_MINUTES = 15;

    public static class Quest {
        private final String title;
        private final String detail;
        private final boolean done;
        private final String link;
        private final String icon;

        public Quest(String title, String detail, boolean done, String link, String icon) {
            this.title = title;
            this.detail = detail;
            this.done = done;
            this.link = link;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public String getDetail() { return detail; }
        public boolean isDone() { return done; }
        public String getLink() { return link; }
        public String getIcon() { return icon; }
    }

    public static List<Quest> build(int joined, int pending, boolean anyCheckInToday, boolean allMetToday,
                                    int focusMinutesToday, boolean reflectedToday) {
        List<Quest> quests = new ArrayList<>();

        // 1. check in
        if (joined == 0) {
            quests.add(new Quest("Check in today", "Join a challenge first", false, "/challenges", "✅"));
        } else if (pending == 0) {
            quests.add(new Quest("Check in today", "All challenges checked in", true, "/checkin", "✅"));
        } else {
            quests.add(new Quest("Check in today", pending + (pending == 1 ? " challenge is" : " challenges are")
                    + " waiting", false, "/checkin", "✅"));
        }

        // 2. meet the goal
        boolean met = anyCheckInToday && allMetToday;
        String metDetail = !anyCheckInToday ? "Check in to find out"
                : (met ? "You met your goal today" : "Not today, try again tomorrow");
        quests.add(new Quest("Meet your goal", metDetail, met, "/checkin", "🎯"));

        // 3. focus
        int shown = Math.min(focusMinutesToday, FOCUS_TARGET_MINUTES);
        quests.add(new Quest("Focus for " + FOCUS_TARGET_MINUTES + " minutes",
                shown + "/" + FOCUS_TARGET_MINUTES + " min",
                focusMinutesToday >= FOCUS_TARGET_MINUTES, "/focus", "⏱️"));

        // 4. reflection
        quests.add(new Quest("Write a reflection", reflectedToday ? "Reflection saved" : "How did today feel?",
                reflectedToday, "/reflections", "📝"));
        return quests;
    }

    public static int doneCount(List<Quest> quests) {
        int n = 0;
        for (Quest q : quests) {
            if (q.isDone()) {
                n++;
            }
        }
        return n;
    }
}
