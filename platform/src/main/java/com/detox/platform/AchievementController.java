package com.detox.platform;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AchievementController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    public AchievementController(BadgeService badgeService, UserRepository userRepository) {
        this.badgeService = badgeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/achievements")
    public String achievements(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("a", badgeService.evaluate(user));
        return "achievements";
    }
}
