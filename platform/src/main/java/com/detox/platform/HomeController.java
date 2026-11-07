package com.detox.platform;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "dashboard";
    }
}
