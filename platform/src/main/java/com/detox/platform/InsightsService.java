package com.detox.platform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Loads the user's data and hands it to InsightsCalc. No new tables are needed. */
@Service
public class InsightsService {

    public static final int HEATMAP_WEEKS = 12;

    private final CheckInRepository checkInRepository;
    private final ReflectionRepository reflectionRepository;
    private final ParticipationRepository participationRepository;
    private final ProgressService progressService;

    public InsightsService(CheckInRepository checkInRepository,
                           ReflectionRepository reflectionRepository,
                           ParticipationRepository participationRepository,
                           ProgressService progressService) {
        this.checkInRepository = checkInRepository;
        this.reflectionRepository = reflectionRepository;
        this.participationRepository = participationRepository;
        this.progressService = progressService;
    }

    /** One entry per day the user checked in (averaged over their challenges). */
    public Map<LocalDate, InsightsCalc.DayStat> dayStats(User user) {
        Map<LocalDate, List<CheckIn>> byDate = new HashMap<>();
        for (CheckIn c : checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user)) {
            byDate.computeIfAbsent(c.getCheckInDate(), k -> new ArrayList<>()).add(c);
        }
        Map<LocalDate, InsightsCalc.DayStat> result = new HashMap<>();
        for (Map.Entry<LocalDate, List<CheckIn>> e : byDate.entrySet()) {
            int sum = 0;
            boolean allMet = true;
            for (CheckIn c : e.getValue()) {
                sum += c.getScreenTimeMinutes();
                if (!c.isGoalMet()) {
                    allMet = false;
                }
            }
            int avg = Math.round((float) sum / e.getValue().size());
            result.put(e.getKey(), new InsightsCalc.DayStat(avg, allMet));
        }
        return result;
    }

    public InsightsCalc.Weekly weekly(User user) {
        int goal = user.getDailyScreenTimeGoal() == null ? 0 : user.getDailyScreenTimeGoal();
        return InsightsCalc.weekly(dayStats(user), goal, LocalDate.now());
    }

    public List<String> coachMessages(User user) {
        LocalDate today = LocalDate.now();
        List<Participation> participations = participationRepository.findByUser(user);
        boolean joinedBeforeYesterday = participations.stream()
                .anyMatch(p -> !p.getJoinedAt().toLocalDate().isAfter(today.minusDays(1)));
        List<ProgressService.ProgressRow> rows = progressService.buildRows(user);
        int current = rows.stream().mapToInt(ProgressService.ProgressRow::getCurrentStreak).max().orElse(0);
        int longest = rows.stream().mapToInt(ProgressService.ProgressRow::getLongestStreak).max().orElse(0);
        return InsightsCalc.coach(dayStats(user), !participations.isEmpty(),
                joinedBeforeYesterday, current, longest, today);
    }

    public InsightsCalc.MoodReport moodReport(User user) {
        List<InsightsCalc.MoodEntry> entries = new ArrayList<>();
        for (Reflection r : reflectionRepository.findByUserOrderByCreatedAtDesc(user)) {
            entries.add(new InsightsCalc.MoodEntry(r.getReflectionDate(), r.getMood()));
        }
        return InsightsCalc.mood(entries, dayStats(user));
    }

    public TriggerCalc.Report triggerReport(User user) {
        List<String> tags = new ArrayList<>();
        for (CheckIn c : checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user)) {
            tags.add(c.getTriggerTag());
        }
        return TriggerCalc.breakdown(tags);
    }

    public InsightsCalc.Heatmap heatmap(User user) {
        return InsightsCalc.heatmap(dayStats(user), LocalDate.now(), HEATMAP_WEEKS);
    }
}
