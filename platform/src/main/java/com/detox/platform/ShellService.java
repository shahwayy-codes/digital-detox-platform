package com.detox.platform;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Numbers for the side panel and top bar: streak, points, level and daily quests. */
@Service
public class ShellService {

    public static class Stats {
        private final int streak;
        private final int points;
        private final int level;
        private final int levelPercent;
        private final int pointsToNext;
        private final int pendingCheckIns;
        private final List<ProgressService.ProgressRow> rows;
        private final List<QuestCalc.Quest> quests;

        public Stats(int streak, int points, int level, int levelPercent, int pointsToNext, int pendingCheckIns,
                     List<ProgressService.ProgressRow> rows, List<QuestCalc.Quest> quests) {
            this.streak = streak;
            this.points = points;
            this.level = level;
            this.levelPercent = levelPercent;
            this.pointsToNext = pointsToNext;
            this.pendingCheckIns = pendingCheckIns;
            this.rows = rows;
            this.quests = quests;
        }

        public int getStreak() { return streak; }
        public int getPoints() { return points; }
        public int getLevel() { return level; }
        public int getLevelPercent() { return levelPercent; }
        public int getPointsToNext() { return pointsToNext; }
        public int getPendingCheckIns() { return pendingCheckIns; }
        public List<ProgressService.ProgressRow> getRows() { return rows; }
        public List<QuestCalc.Quest> getQuests() { return quests; }
        public int getQuestsDone() { return QuestCalc.doneCount(quests); }
        public int getQuestsTotal() { return quests.size(); }
    }

    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;
    private final ReflectionRepository reflectionRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final ProgressService progressService;
    private final BadgeService badgeService;

    public ShellService(ParticipationRepository participationRepository,
                        CheckInRepository checkInRepository,
                        ReflectionRepository reflectionRepository,
                        FocusSessionRepository focusSessionRepository,
                        ProgressService progressService,
                        BadgeService badgeService) {
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
        this.reflectionRepository = reflectionRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.progressService = progressService;
        this.badgeService = badgeService;
    }

    public Stats statsFor(User user) {
        LocalDate today = LocalDate.now();
        List<Participation> joined = participationRepository.findByUser(user);
        List<ProgressService.ProgressRow> rows = progressService.buildRows(user);
        int streak = rows.stream().mapToInt(ProgressService.ProgressRow::getCurrentStreak).max().orElse(0);
        BadgeService.Achievements a = badgeService.evaluate(user);

        List<CheckIn> todays = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user).stream()
                .filter(c -> c.getCheckInDate().equals(today))
                .collect(Collectors.toList());
        int pending = (int) joined.stream()
                .filter(p -> todays.stream().noneMatch(c -> c.getParticipation().getId().equals(p.getId())))
                .count();
        boolean anyToday = !todays.isEmpty();
        boolean allMet = anyToday && todays.stream().allMatch(CheckIn::isGoalMet);

        int focusToday = focusSessionRepository.findByUserAndStatus(user, FocusSession.COMPLETED).stream()
                .filter(s -> s.getEndedAt() != null && s.getEndedAt().toLocalDate().equals(today))
                .mapToInt(FocusSession::getPlannedMinutes).sum();
        boolean reflected = reflectionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .anyMatch(r -> today.equals(r.getReflectionDate()));

        List<QuestCalc.Quest> quests = QuestCalc.build(joined.size(), pending, anyToday, allMet, focusToday, reflected);
        return new Stats(streak, a.getPoints(), a.getLevel(), a.getLevelPercent(), a.getPointsToNext(),
                pending, rows, quests);
    }
}
