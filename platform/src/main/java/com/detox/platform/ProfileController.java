package com.detox.platform;

import java.security.Principal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        model.addAttribute("user", currentUser(principal));
        return "profile";
    }

    @PostMapping("/profile")
    public String update(@RequestParam String fullName,
                         @RequestParam(required = false) Integer dailyGoalMinutes,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        String name = fullName.trim();
        if (name.isEmpty() || name.length() > 60) {
            redirectAttributes.addFlashAttribute("error", "Name is required and must be 60 characters or less.");
            return "redirect:/profile";
        }
        if (dailyGoalMinutes != null && (dailyGoalMinutes < 1 || dailyGoalMinutes > 1440)) {
            redirectAttributes.addFlashAttribute("error", "Daily goal must be between 1 and 1440 minutes.");
            return "redirect:/profile";
        }
        User user = currentUser(principal);
        user.setFullName(name);
        user.setDailyScreenTimeGoal(dailyGoalMinutes);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
        } else if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "New password must be at least 8 characters.");
        } else if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
        } else {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Password changed.");
        }
        return "redirect:/profile";
    }
}
