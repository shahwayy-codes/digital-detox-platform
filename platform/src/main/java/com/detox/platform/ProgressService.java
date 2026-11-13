package com.detox.platform;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    public static class ProgressRow {
        private final Long challengeId;
        private final String title;
        private final int durationDays;
        private final int daysCheckedIn;
        private final int goalsMet;
        private final int currentStreak;
        private final int longestStreak;
        private final int avgMinutes;
        private final int percentComplete;
        private final boolean checkedInToday;

        public ProgressRow(Long challengeId, String title, int durationDays, int daysCheckedIn, int goalsMet,
                           int currentStreak, int longestStreak, int avgMinutes, int percentComplete,
                           boolean checkedInToday) {
            this.challengeId = challengeId;
            this.title = title;
            this.durationDays = durationDays;
            this.daysCheckedIn = daysCheckedIn;
            this.goalsMet = goalsMet;
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
            this.avgMinutes = avgMinutes;
            this.percentComplete = percentComplete;
            this.checkedInToday = checkedInToday;
        }

        public Long getChallengeId() { return challengeId; }
        public String getTitle() { return title; }
        public int getDurationDays() { return durationDays; }
        public int getDaysCheckedIn() { return daysCheckedIn; }
        public int getGoalsMet() { return goalsMet; }
        public int getCurrentStreak() { return currentStreak; }
        public int getLongestStreak() { return longestStreak; }
        public int getAvgMinutes() { return avgMinutes; }
        public int getPercentComplete() { return percentComplete; }
        public boolean isCheckedInToday() { return checkedInToday; }
    }

    public static class DayBar {
        private final String label;
        private final int minutes;
        private final int percent;
        private final boolean checkedIn;

        public DayBar(String label, int minutes, int percent, boolean checkedIn) {
            this.label = label;
            this.minutes = minutes;
            this.percent = percent;
            this.checkedIn = checkedIn;
        }

        public String getLabel() { return label; }
        public int getMinutes() { return minutes; }
        public int getPercent() { return percent; }
        public boolean isCheckedIn() { return checkedIn; }
    }

    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;

    public ProgressService(ParticipationRepository participationRepository,
                           CheckInRepository checkInRepository) {
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
    }

    public List<ProgressRow> buildRows(User user) {
        LocalDate today = LocalDate.now();
        Map<Long, List<CheckIn>> byParticipation = checkInRepository
                .findByParticipation_UserOrderByCheckInDateDesc(user)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getParticipation().getId()));

        List<ProgressRow> rows = new ArrayList<>();
        for (Participation p : participationRepository.findByUser(user)) {
            List<CheckIn> list = byParticipation.getOrDefault(p.getId(), new ArrayList<>());

            TreeSet<LocalDate> metDates = new TreeSet<>();
            boolean checkedToday = false;
            int totalMinutes = 0;
            for (CheckIn c : list) {
                if (c.isGoalMet()) {
                    metDates.add(c.getCheckInDate());
                }
                if (c.getCheckInDate().equals(today)) {
                    checkedToday = true;
                }
                totalMinutes += c.getScreenTimeMinutes();
            }

            int avg = list.isEmpty() ? 0 : Math.round((float) totalMinutes / list.size());
            int duration = Math.max(1, p.getChallenge().getDurationDays());
            int percent = Math.min(100, metDates.size() * 100 / duration);

            rows.add(new ProgressRow(
                    p.getChallenge().getId(),
                    p.getChallenge().getTitle(),
                    duration,
                    list.size(),
                    metDates.size(),
                    currentStreak(metDates, today),
                    longestStreak(metDates),
                    avg,
                    percent,
                    checkedToday));
        }
        return rows;
    }

    private int currentStreak(TreeSet<LocalDate> metDates, LocalDate today) {
        LocalDate cursor = metDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (metDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int longestStreak(TreeSet<LocalDate> metDates) {
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate d : metDates) {
            if (previous != null && previous.plusDays(1).equals(d)) {
                run++;
            } else {
                run = 1;
            }
            longest = Math.max(longest, run);
            previous = d;
        }
        return longest;
    }

    public List<DayBar> lastSevenDays(User user) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, List<CheckIn>> byDate = checkInRepository
                .findByParticipation_UserOrderByCheckInDateDesc(user)
                .stream()
                .collect(Collectors.groupingBy(CheckIn::getCheckInDate));

        int[] minutes = new int[7];
        boolean[] has = new boolean[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            List<CheckIn> list = byDate.getOrDefault(d, new ArrayList<>());
            has[i] = !list.isEmpty();
            if (!list.isEmpty()) {
                int sum = list.stream().mapToInt(CheckIn::getScreenTimeMinutes).sum();
                minutes[i] = Math.round((float) sum / list.size());
            }
        }

        int max = Arrays.stream(minutes).max().orElse(0);
        List<DayBar> bars = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    + " " + d.getDayOfMonth();
            int percent = max == 0 ? 0 : minutes[i] * 100 / max;
            bars.add(new DayBar(label, minutes[i], percent, has[i]));
        }
        return bars;
    }
}
