package com.detox.platform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecommendService {

    private final ChallengeRepository challengeRepository;
    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;

    public RecommendService(ChallengeRepository challengeRepository,
                            ParticipationRepository participationRepository,
                            CheckInRepository checkInRepository) {
        this.challengeRepository = challengeRepository;
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
    }

    public List<RecommendCalc.Pick> forUser(User user, int max) {
        List<Participation> joined = participationRepository.findByUser(user);
        Set<Long> joinedIds = new HashSet<>();
        Set<String> joinedCategories = new HashSet<>();
        for (Participation p : joined) {
            joinedIds.add(p.getChallenge().getId());
            joinedCategories.add(p.getChallenge().getCategory());
        }

        List<CheckIn> checkIns = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user);
        int avg = checkIns.isEmpty() ? 0
                : Math.round((float) checkIns.stream().mapToInt(CheckIn::getScreenTimeMinutes).sum() / checkIns.size());

        List<RecommendCalc.Cand> candidates = challengeRepository.findAll().stream()
                .filter(c -> !joinedIds.contains(c.getId()))
                .map(c -> new RecommendCalc.Cand(c.getId(), c.getTitle(), c.getCategory(), c.getDurationDays()))
                .collect(Collectors.toList());

        return RecommendCalc.recommend(candidates, joinedCategories, avg, checkIns.size(), max);
    }
}
