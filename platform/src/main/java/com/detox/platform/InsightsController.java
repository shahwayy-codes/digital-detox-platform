package com.detox.platform;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InsightsController {

    private final InsightsService insightsService;
    private final UserRepository userRepository;

    public InsightsController(InsightsService insightsService, UserRepository userRepository) {
        this.insightsService = insightsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/insights")
    public String insights(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("weekly", insightsService.weekly(user));
        model.addAttribute("mood", insightsService.moodReport(user));
        model.addAttribute("coach", insightsService.coachMessages(user));
        model.addAttribute("triggers", insightsService.triggerReport(user));
        return "insights";
    }
}
