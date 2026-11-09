package com.detox.platform;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository requestRepository;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetRequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/forgot-password")
    public String page() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String request(@RequestParam String email, RedirectAttributes redirectAttributes) {
        String clean = email.trim().toLowerCase();
        if (userRepository.existsByEmail(clean) && !requestRepository.existsByEmailAndHandledFalse(clean)) {
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail(clean);
            requestRepository.save(request);
        }
        redirectAttributes.addFlashAttribute("message",
                "If an account with that email exists, an admin has been notified and will help you reset your password.");
        return "redirect:/login";
    }
}
