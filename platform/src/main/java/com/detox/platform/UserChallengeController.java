package com.detox.platform;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserChallengeController {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public UserChallengeController(ChallengeRepository challengeRepository, UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/challenges/new")
    public String form(Model model) {
        model.addAttribute("challengeForm", new ChallengeForm());
        return "challenge-form";
    }

    @PostMapping("/challenges")
    public String create(@Valid @ModelAttribute("challengeForm") ChallengeForm form,
                         BindingResult result,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "challenge-form";
        }
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        Challenge challenge = new Challenge();
        challenge.setTitle(form.getTitle().trim());
        challenge.setCategory(form.getCategory().trim());
        challenge.setDescription(form.getDescription().trim());
        challenge.setDailyGoal(form.getDailyGoal().trim());
        challenge.setDurationDays(form.getDurationDays());
        challenge.setCreatedBy(user);
        challengeRepository.save(challenge);

        redirectAttributes.addFlashAttribute("message", "Your challenge was created. Other users can now join it.");
        return "redirect:/challenges/" + challenge.getId();
    }
}
