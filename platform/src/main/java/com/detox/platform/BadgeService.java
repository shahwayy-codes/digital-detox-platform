package com.detox.platform;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {

    public static class Badge {
        private final String name;
        private final String description;
        private final boolean earned;

        public Badge(String name, String description, boolean earned) {
            this.name = name;
            this.description = description;
            this.earned = earned;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isEarned() { return earned; }
    }

    public static class Achievements {
        private final int points;
        private final int level;
        private final int levelPercent;
        private final int pointsToNext;
        private final int earnedCount;
        private final List<Badge> badges;

        public Achievements(int points, int level, int levelPercent, int pointsToNext,
                            int earnedCount, List<Badge> badges) {
            this.points = points;
            this.level = level;
            this.levelPercent = levelPercent;
            this.pointsToNext = pointsToNext;
            this.earnedCount = earnedCount;
            this.badges = badges;
        }

        public int getPoints() { return points; }
        public int getLevel() { return level; }
        public int getLevelPercent() { return levelPercent; }
        public int getPointsToNext() { return pointsToNext; }
        public int getEarnedCount() { return earnedCount; }
        public List<Badge> getBadges() { return badges; }
    }

    private static final int POINTS_PER_LEVEL = 50;

    private final ProgressService progressService;
    private final CheckInRepository checkInRepository;
    private final ReflectionRepository reflectionRepository;

    public BadgeService(ProgressService progressService,
                        CheckInRepository checkInRepository,
                        ReflectionRepository reflectionRepository) {
        this.progressService = progressService;
        this.checkInRepository = checkInRepository;
        this.reflectionRepository = reflectionRepository;
    }

    public Achievements evaluate(User user) {
        List<ProgressService.ProgressRow> rows = progressService.buildRows(user);
        List<CheckIn> checkIns = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user);
        long reflections = reflectionRepository.countByUser(user);

        int totalCheckIns = checkIns.size();
        int goalsMet = (int) checkIns.stream().filter(CheckIn::isGoalMet).count();
        int longest = rows.stream().mapToInt(ProgressService.ProgressRow::getLongestStreak).max().orElse(0);
        boolean finished = rows.stream().anyMatch(r -> r.getGoalsMet() >= r.getDurationDays());
        boolean lightDay = checkIns.stream().anyMatch(c -> c.getScreenTimeMinutes() <= 120);

        int points = totalCheckIns * 5 + goalsMet * 10 + (int) (reflections * 5);

        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("First Step", "Complete your first daily check-in.", totalCheckIns >= 1));
        badges.add(new Badge("3-Day Streak", "Meet your daily goal 3 days in a row.", longest >= 3));
        badges.add(new Badge("7-Day Streak", "Meet your daily goal 7 days in a row.", longest >= 7));
        badges.add(new Badge("Goal Getter", "Meet your daily goal 10 times.", goalsMet >= 10));
        badges.add(new Badge("Consistent", "Complete 20 check-ins.", totalCheckIns >= 20));
        badges.add(new Badge("Challenge Finisher", "Meet the goal on every day of a challenge.", finished));
        badges.add(new Badge("Light User", "Log a day with 2 hours of screen time or less.", lightDay));
        badges.add(new Badge("Mindful Writer", "Write your first reflection.", reflections >= 1));

        int earned = (int) badges.stream().filter(Badge::isEarned).count();
        int level = points / POINTS_PER_LEVEL + 1;
        int intoLevel = points % POINTS_PER_LEVEL;
        int percent = intoLevel * 100 / POINTS_PER_LEVEL;

        return new Achievements(points, level, percent, POINTS_PER_LEVEL - intoLevel, earned, badges);
    }
}
