package com.detox.platform;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminChallengeController {

    private final ChallengeRepository challengeRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final CommentRepository commentRepository;

    public AdminChallengeController(ChallengeRepository challengeRepository,
                                    ParticipationRepository participationRepository,
                                    UserRepository userRepository,
                                    CheckInRepository checkInRepository,
                                    CommentRepository commentRepository) {
        this.challengeRepository = challengeRepository;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/admin/challenges")
    public String list(Model model) {
        List<Challenge> challenges = challengeRepository.findAll();
        Map<Long, Long> counts = new HashMap<>();
        for (Challenge c : challenges) {
            counts.put(c.getId(), participationRepository.countByChallenge(c));
        }
        model.addAttribute("challenges", challenges);
        model.addAttribute("counts", counts);
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("challengeCount", challengeRepository.count());
        model.addAttribute("joinCount", participationRepository.count());
        model.addAttribute("checkInCount", checkInRepository.count());
        return "admin-challenges";
    }

    @GetMapping("/admin/challenges/new")
    public String newForm(Model model) {
        model.addAttribute("challengeForm", new ChallengeForm());
        model.addAttribute("formAction", "/admin/challenges");
        model.addAttribute("formTitle", "Add challenge");
        return "admin-challenge-form";
    }

    @PostMapping("/admin/challenges")
    public String create(@Valid @ModelAttribute("challengeForm") ChallengeForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/admin/challenges");
            model.addAttribute("formTitle", "Add challenge");
            return "admin-challenge-form";
        }
        Challenge challenge = new Challenge();
        apply(form, challenge);
        challengeRepository.save(challenge);
        redirectAttributes.addFlashAttribute("message", "Challenge added.");
        return "redirect:/admin/challenges";
    }

    @GetMapping("/admin/challenges/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/admin/challenges";
        }
        ChallengeForm form = new ChallengeForm();
        form.setTitle(challenge.getTitle());
        form.setCategory(challenge.getCategory());
        form.setDescription(challenge.getDescription());
        form.setDailyGoal(challenge.getDailyGoal());
        form.setDurationDays(challenge.getDurationDays());
        model.addAttribute("challengeForm", form);
        model.addAttribute("formAction", "/admin/challenges/" + id);
        model.addAttribute("formTitle", "Edit challenge");
        return "admin-challenge-form";
    }

    @PostMapping("/admin/challenges/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("challengeForm") ChallengeForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/admin/challenges";
        }
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/admin/challenges/" + id);
            model.addAttribute("formTitle", "Edit challenge");
            return "admin-challenge-form";
        }
        apply(form, challenge);
        challengeRepository.save(challenge);
        redirectAttributes.addFlashAttribute("message", "Challenge updated.");
        return "redirect:/admin/challenges";
    }

    @PostMapping("/admin/challenges/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Challenge challenge = challengeRepository.findById(id).orElse(null);
        if (challenge == null) {
            return "redirect:/admin/challenges";
        }
        if (participationRepository.countByChallenge(challenge) > 0) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete this challenge because users have already joined it.");
        } else {
            commentRepository.deleteAll(commentRepository.findByChallengeOrderByCreatedAtDesc(challenge));
            challengeRepository.delete(challenge);
            redirectAttributes.addFlashAttribute("message", "Challenge deleted.");
        }
        return "redirect:/admin/challenges";
    }

    private void apply(ChallengeForm form, Challenge challenge) {
        challenge.setTitle(form.getTitle().trim());
        challenge.setCategory(form.getCategory().trim());
        challenge.setDescription(form.getDescription().trim());
        challenge.setDailyGoal(form.getDailyGoal().trim());
        challenge.setDurationDays(form.getDurationDays());
    }
}
