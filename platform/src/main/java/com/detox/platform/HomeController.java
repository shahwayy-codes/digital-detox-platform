package com.detox.platform;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final CheckInRepository checkInRepository;

    public HomeController(UserRepository userRepository,
                          ParticipationRepository participationRepository,
                          CheckInRepository checkInRepository) {
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.checkInRepository = checkInRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<Participation> participations = participationRepository.findByUser(user);

        LocalDate today = LocalDate.now();
        int pending = 0;
        for (Participation p : participations) {
            if (!checkInRepository.existsByParticipationAndCheckInDate(p, today)) {
                pending++;
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("isAdmin", "ROLE_ADMIN".equals(user.getRole()));
        model.addAttribute("participations", participations);
        model.addAttribute("pendingCheckIns", pending);
        return "dashboard";
    }
}
