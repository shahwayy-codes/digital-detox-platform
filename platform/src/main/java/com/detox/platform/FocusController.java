package com.detox.platform;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FocusController {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;

    public FocusController(FocusSessionRepository focusSessionRepository, UserRepository userRepository) {
        this.focusSessionRepository = focusSessionRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/focus")
    public String page(Principal principal, Model model) {
        User user = currentUser(principal);
        FocusSession active = focusSessionRepository
                .findFirstByUserAndStatusOrderByStartedAtDesc(user, FocusSession.RUNNING).orElse(null);

        List<FocusSession> done = focusSessionRepository.findByUserAndStatus(user, FocusSession.COMPLETED);
        int totalMinutes = done.stream().mapToInt(FocusSession::getPlannedMinutes).sum();
        LocalDate today = LocalDate.now();
        int todayMinutes = done.stream()
                .filter(s -> s.getEndedAt() != null && s.getEndedAt().toLocalDate().equals(today))
                .mapToInt(FocusSession::getPlannedMinutes).sum();

        model.addAttribute("options", FocusRules.OPTIONS);
        model.addAttribute("active", active);
        if (active != null) {
            model.addAttribute("remainingSeconds",
                    FocusRules.remainingSeconds(active.getStartedAt(), active.getPlannedMinutes(), LocalDateTime.now()));
        }
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("totalSessions", done.size());
        model.addAttribute("todayMinutes", todayMinutes);
        model.addAttribute("focusPoints", FocusRules.points(totalMinutes));
        model.addAttribute("recent", focusSessionRepository.findTop10ByUserOrderByStartedAtDesc(user));
        return "focus";
    }

    @PostMapping("/focus/start")
    public String start(@RequestParam int minutes, Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        if (!FocusRules.validDuration(minutes)) {
            redirectAttributes.addFlashAttribute("error", "Choose 15, 30 or 60 minutes.");
            return "redirect:/focus";
        }
        if (focusSessionRepository.findFirstByUserAndStatusOrderByStartedAtDesc(user, FocusSession.RUNNING).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "You already have a focus session running.");
            return "redirect:/focus";
        }
        FocusSession session = new FocusSession();
        session.setUser(user);
        session.setPlannedMinutes(minutes);
        session.setStartedAt(LocalDateTime.now());
        focusSessionRepository.save(session);
        return "redirect:/focus";
    }

    @PostMapping("/focus/complete")
    public String complete(Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        FocusSession active = focusSessionRepository
                .findFirstByUserAndStatusOrderByStartedAtDesc(user, FocusSession.RUNNING).orElse(null);
        if (active == null) {
            redirectAttributes.addFlashAttribute("error", "There is no running focus session.");
            return "redirect:/focus";
        }
        LocalDateTime now = LocalDateTime.now();
        if (!FocusRules.canComplete(active.getStartedAt(), active.getPlannedMinutes(), now)) {
            redirectAttributes.addFlashAttribute("error", "The timer is not finished yet.");
            return "redirect:/focus";
        }
        active.setStatus(FocusSession.COMPLETED);
        active.setEndedAt(now);
        focusSessionRepository.save(active);
        redirectAttributes.addFlashAttribute("message", "Well done! " + active.getPlannedMinutes()
                + " minutes of focus completed. You earned " + FocusRules.points(active.getPlannedMinutes()) + " points.");
        return "redirect:/focus";
    }

    @PostMapping("/focus/cancel")
    public String cancel(Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        focusSessionRepository.findFirstByUserAndStatusOrderByStartedAtDesc(user, FocusSession.RUNNING)
                .ifPresent(s -> {
                    s.setStatus(FocusSession.CANCELLED);
                    s.setEndedAt(LocalDateTime.now());
                    focusSessionRepository.save(s);
                });
        redirectAttributes.addFlashAttribute("message", "Focus session stopped. No points for stopped sessions.");
        return "redirect:/focus";
    }
}
