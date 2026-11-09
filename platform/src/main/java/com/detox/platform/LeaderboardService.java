package com.detox.platform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    public static class Entry {
        private int rank;
        private final String name;
        private final int weeklyPoints;
        private final int weeklyCheckIns;
        private final int streak;
        private final boolean you;

        public Entry(String name, int weeklyPoints, int weeklyCheckIns, int streak, boolean you) {
            this.name = name;
            this.weeklyPoints = weeklyPoints;
            this.weeklyCheckIns = weeklyCheckIns;
            this.streak = streak;
            this.you = you;
        }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public String getName() { return name; }
        public int getWeeklyPoints() { return weeklyPoints; }
        public int getWeeklyCheckIns() { return weeklyCheckIns; }
        public int getStreak() { return streak; }
        public boolean isYou() { return you; }
    }

    private final GroupMemberRepository groupMemberRepository;
    private final CheckInRepository checkInRepository;
    private final ProgressService progressService;

    public LeaderboardService(GroupMemberRepository groupMemberRepository,
                              CheckInRepository checkInRepository,
                              ProgressService progressService) {
        this.groupMemberRepository = groupMemberRepository;
        this.checkInRepository = checkInRepository;
        this.progressService = progressService;
    }

    public List<Entry> weekly(FriendGroup group, User viewer) {
        LocalDate since = LocalDate.now().minusDays(6);
        List<Entry> entries = new ArrayList<>();

        for (GroupMember member : groupMemberRepository.findByGroup(group)) {
            User u = member.getUser();
            List<CheckIn> thisWeek = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(u)
                    .stream()
                    .filter(c -> !c.getCheckInDate().isBefore(since))
                    .collect(Collectors.toList());

            int points = 0;
            for (CheckIn c : thisWeek) {
                points += 5 + (c.isGoalMet() ? 10 : 0);
            }
            int streak = progressService.buildRows(u).stream()
                    .mapToInt(ProgressService.ProgressRow::getCurrentStreak)
                    .max()
                    .orElse(0);

            entries.add(new Entry(u.getFullName(), points, thisWeek.size(), streak,
                    u.getId().equals(viewer.getId())));
        }

        entries.sort(Comparator.comparingInt(Entry::getWeeklyPoints).reversed()
                .thenComparing(Entry::getName));
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return entries;
    }
}
