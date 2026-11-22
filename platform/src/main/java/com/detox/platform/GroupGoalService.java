package com.detox.platform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GroupGoalService {

    private final GroupMemberRepository groupMemberRepository;
    private final InsightsService insightsService;

    public GroupGoalService(GroupMemberRepository groupMemberRepository, InsightsService insightsService) {
        this.groupMemberRepository = groupMemberRepository;
        this.insightsService = insightsService;
    }

    public GroupGoalCalc.Result weekly(FriendGroup group) {
        List<Map<LocalDate, InsightsCalc.DayStat>> days = new ArrayList<>();
        List<Integer> goals = new ArrayList<>();
        for (GroupMember member : groupMemberRepository.findByGroup(group)) {
            User u = member.getUser();
            days.add(insightsService.dayStats(u));
            goals.add(u.getDailyScreenTimeGoal() == null ? 0 : u.getDailyScreenTimeGoal());
        }
        return GroupGoalCalc.compute(days, goals, LocalDate.now());
    }
}
