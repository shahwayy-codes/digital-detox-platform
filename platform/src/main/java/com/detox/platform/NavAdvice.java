package com.detox.platform;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavAdvice {

    private final UserRepository userRepository;
    private final ShellService shellService;

    public NavAdvice(UserRepository userRepository, ShellService shellService) {
        this.userRepository = userRepository;
        this.shellService = shellService;
    }

    @ModelAttribute("navUser")
    public User navUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }

    /**
     * Streak, points and daily quests for the side panel. Only built for normal page views
     * (GET requests), and never allowed to break a page.
     */
    @ModelAttribute("navStats")
    public ShellService.Stats navStats(Principal principal, HttpServletRequest request) {
        if (principal == null || !"GET".equals(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/admin") || path.startsWith("/export")) {
            return null;
        }
        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            return user == null ? null : shellService.statsFor(user);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
