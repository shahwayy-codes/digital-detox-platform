package com.detox.platform;

import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        String email = form.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            result.rejectValue("email", "exists", "This email is already registered");
        }
        if (result.hasErrors()) {
            return "register";
        }
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("registered", true);
        return "redirect:/login";
    }
}
