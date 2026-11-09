package com.detox.platform;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LeaveChallengeController {

    private final ChallengeRepository challengeRepository;
    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    public LeaveChallengeController(ChallengeRepository challengeRepository,
                                    ParticipationRepository participationRepository,
                                    CheckInRepository checkInRepository,
                                    UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/challenges/{id}/leave")
    @Transactional
    public String leave(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/challenges";
        }

        Participation participation = participationRepository.findByUser(user).stream()
                .filter(p -> p.getChallenge().getId().equals(id))
                .findFirst()
                .orElse(null);

        if (participation != null) {
            Long participationId = participation.getId();
            List<CheckIn> mine = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user).stream()
                    .filter(c -> c.getParticipation().getId().equals(participationId))
                    .collect(Collectors.toList());
            checkInRepository.deleteAll(mine);
            participationRepository.delete(participation);
            redirectAttributes.addFlashAttribute("message", "You left the challenge.");
        }
        return "redirect:/challenges/" + id;
    }
}
