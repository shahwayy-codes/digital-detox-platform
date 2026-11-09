package com.detox.platform;

import java.security.SecureRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminResetController {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final PasswordResetRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AdminResetController(PasswordResetRequestRepository requestRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/resets")
    public String list(Model model) {
        model.addAttribute("requests", requestRepository.findByHandledFalseOrderByCreatedAtDesc());
        return "admin-resets";
    }

    @PostMapping("/admin/resets/{id}/reset")
    public String reset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        PasswordResetRequest request = requestRepository.findById(id).orElse(null);
        if (request == null) {
            return "redirect:/admin/resets";
        }
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            request.setHandled(true);
            requestRepository.save(request);
            redirectAttributes.addFlashAttribute("error", "That account no longer exists.");
            return "redirect:/admin/resets";
        }
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            temp.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        user.setPassword(passwordEncoder.encode(temp.toString()));
        userRepository.save(user);
        request.setHandled(true);
        requestRepository.save(request);

        redirectAttributes.addFlashAttribute("message",
                "Temporary password for " + user.getEmail() + ": " + temp
                        + "  Give it to the user and ask them to change it on their profile page.");
        return "redirect:/admin/resets";
    }

    @PostMapping("/admin/resets/{id}/dismiss")
    public String dismiss(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        PasswordResetRequest request = requestRepository.findById(id).orElse(null);
        if (request != null) {
            request.setHandled(true);
            requestRepository.save(request);
            redirectAttributes.addFlashAttribute("message", "Request dismissed.");
        }
        return "redirect:/admin/resets";
    }
}
