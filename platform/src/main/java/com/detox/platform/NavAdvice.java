package com.detox.platform;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavAdvice {

    private final UserRepository userRepository;

    public NavAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("navUser")
    public User navUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}
