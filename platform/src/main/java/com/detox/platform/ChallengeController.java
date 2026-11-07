package com.detox.platform;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;

    public ChallengeController(ChallengeRepository challengeRepository,
                               ParticipationRepository participationRepository,
                               UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/challenges")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       Principal principal,
                       Model model) {

        String keyword = q == null ? "" : q.trim().toLowerCase();
        String selected = category == null ? "" : category.trim();

        List<Challenge> all = challengeRepository.findAll();

        List<Challenge> filtered = all.stream()
                .filter(c -> keyword.isEmpty()
                        || c.getTitle().toLowerCase().contains(keyword)
                        || c.getDescription().toLowerCase().contains(keyword))
                .filter(c -> selected.isEmpty() || c.getCategory().equalsIgnoreCase(selected))
                .collect(Collectors.toList());

        List<String> categories = all.stream()
                .map(Challenge::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        User user = currentUser(principal);
        Set<Long> joinedIds = participationRepository.findByUser(user).stream()
                .map(p -> p.getChallenge().getId())
                .collect(Collectors.toSet());

        model.addAttribute("challenges", filtered);
        model.addAttribute("categories", categories);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("selectedCategory", selected);
        model.addAttribute("joinedIds", joinedIds);
        return "challenges";
    }

    @GetMapping("/challenges/{id}")
    public String details(@PathVariable Long id, Principal principal, Model model) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/challenges";
        }
        User user = currentUser(principal);
        model.addAttribute("challenge", challenge);
        model.addAttribute("joined", participationRepository.existsByUserAndChallenge(user, challenge));
        model.addAttribute("participants", participationRepository.countByChallenge(challenge));
        return "challenge-details";
    }

    @PostMapping("/challenges/{id}/join")
    public String join(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/challenges";
        }
        User user = currentUser(principal);
        if (!participationRepository.existsByUserAndChallenge(user, challenge)) {
            participationRepository.save(new Participation(user, challenge));
            redirectAttributes.addFlashAttribute("message", "You joined this challenge. Good luck!");
        }
        return "redirect:/challenges/" + id;
    }
}
