package com.detox.platform;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** A printable certificate for a finished challenge. */
@Controller
public class CertificateController {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private final UserRepository userRepository;
    private final ProgressService progressService;
    private final CheckInRepository checkInRepository;
    private final ChallengeRepository challengeRepository;

    public CertificateController(UserRepository userRepository,
                                 ProgressService progressService,
                                 CheckInRepository checkInRepository,
                                 ChallengeRepository challengeRepository) {
        this.userRepository = userRepository;
        this.progressService = progressService;
        this.checkInRepository = checkInRepository;
        this.challengeRepository = challengeRepository;
    }

    @GetMapping("/certificate/{challengeId}")
    public String certificate(@PathVariable Long challengeId, Principal principal,
                              Model model, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        ProgressService.ProgressRow row = progressService.buildRows(user).stream()
                .filter(r -> r.getChallengeId().equals(challengeId))
                .findFirst().orElse(null);
        if (row == null || row.getGoalsMet() < row.getDurationDays()) {
            redirectAttributes.addFlashAttribute("error",
                    "Meet the goal on every day of a challenge to unlock its certificate.");
            return "redirect:/progress";
        }

        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow();
        LocalDate first = null;
        LocalDate last = null;
        for (CheckIn c : checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user)) {
            if (!c.isGoalMet() || !c.getParticipation().getChallenge().getId().equals(challengeId)) {
                continue;
            }
            LocalDate d = c.getCheckInDate();
            if (first == null || d.isBefore(first)) {
                first = d;
            }
            if (last == null || d.isAfter(last)) {
                last = d;
            }
        }

        model.addAttribute("name", user.getFullName());
        model.addAttribute("challenge", challenge);
        model.addAttribute("goalDays", row.getGoalsMet());
        model.addAttribute("startDate", first == null ? "" : first.format(FORMAT));
        model.addAttribute("endDate", last == null ? "" : last.format(FORMAT));
        model.addAttribute("issuedOn", LocalDate.now().format(FORMAT));
        model.addAttribute("certId", "DDC-" + user.getId() + "-" + challengeId);
        return "certificate";
    }
}
