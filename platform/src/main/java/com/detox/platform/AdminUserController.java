package com.detox.platform;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/users")
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return "redirect:/admin/users";
        }
        if (target.getEmail().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "You cannot disable your own account.");
        } else if ("ROLE_ADMIN".equals(target.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Admin accounts cannot be disabled.");
        } else {
            target.setEnabled(!target.isEnabled());
            userRepository.save(target);
            redirectAttributes.addFlashAttribute("message",
                    target.isEnabled() ? "Account enabled." : "Account disabled.");
        }
        return "redirect:/admin/users";
    }
}
