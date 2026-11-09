package com.detox.platform;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.detox.platform.CheckIn;
import com.detox.platform.CheckInRepository;

@Controller
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;

    public ProgressController(ProgressService progressService,
                              UserRepository userRepository,
                              ParticipationRepository participationRepository,
                              CheckInRepository checkInRepository) {
        this.progressService = progressService;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/progress")
    public String progress(Principal principal, Model model) {
        User user = currentUser(principal);
        List<ProgressService.ProgressRow> rows = progressService.buildRows(user);

        model.addAttribute("rows", rows);
        model.addAttribute("bestCurrent", rows.stream()
                .mapToInt(ProgressService.ProgressRow::getCurrentStreak).max().orElse(0));
        model.addAttribute("bestLongest", rows.stream()
                .mapToInt(ProgressService.ProgressRow::getLongestStreak).max().orElse(0));
        model.addAttribute("totalCheckIns", rows.stream()
                .mapToInt(ProgressService.ProgressRow::getDaysCheckedIn).sum());
        model.addAttribute("totalGoalsMet", rows.stream()
                .mapToInt(ProgressService.ProgressRow::getGoalsMet).sum());
        model.addAttribute("week", progressService.lastSevenDays(user));
        return "progress";
    }

    // TESTING ONLY: adds 4 past days of check-ins to your first joined challenge
    @PostMapping("/progress/demo-data")
    public String demoData(Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        List<Participation> list = participationRepository.findByUser(user);
        if (list.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Join a challenge first.");
            return "redirect:/progress";
        }
        Participation p = list.get(0);
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 4; i++) {
            LocalDate day = today.minusDays(i);
            if (!checkInRepository.existsByParticipationAndCheckInDate(p, day)) {
                CheckIn c = new CheckIn();
                c.setParticipation(p);
                c.setCheckInDate(day);
                c.setGoalMet(true);
                c.setScreenTimeMinutes(90 + i * 20);
                c.setNote("Demo data");
                checkInRepository.save(c);
            }
        }
        redirectAttributes.addFlashAttribute("message", "Demo check-ins added for the last 4 days.");
        return "redirect:/progress";
    }
}
