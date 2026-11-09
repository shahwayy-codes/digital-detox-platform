package com.detox.platform;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReflectionController {

    private static final List<String> MOODS = List.of("Great", "Good", "Okay", "Tough");

    private final ReflectionRepository reflectionRepository;
    private final UserRepository userRepository;

    public ReflectionController(ReflectionRepository reflectionRepository, UserRepository userRepository) {
        this.reflectionRepository = reflectionRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/reflections")
    public String page(Principal principal, Model model) {
        User user = currentUser(principal);
        model.addAttribute("reflections", reflectionRepository.findByUserOrderByCreatedAtDesc(user));
        model.addAttribute("moods", MOODS);
        return "reflections";
    }

    @PostMapping("/reflections")
    public String save(@RequestParam(required = false) String mood,
                       @RequestParam(required = false) String content,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Write something before saving.");
            return "redirect:/reflections";
        }
        if (text.length() > 1000) {
            text = text.substring(0, 1000);
        }
        String chosenMood = (mood != null && MOODS.contains(mood)) ? mood : "Okay";

        Reflection reflection = new Reflection();
        reflection.setUser(currentUser(principal));
        reflection.setMood(chosenMood);
        reflection.setContent(text);
        reflectionRepository.save(reflection);

        redirectAttributes.addFlashAttribute("message", "Reflection saved.");
        return "redirect:/reflections";
    }

    @PostMapping("/reflections/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = currentUser(principal);
        Reflection reflection = reflectionRepository.findById(id).orElse(null);
        if (reflection != null && reflection.getUser().getId().equals(user.getId())) {
            reflectionRepository.delete(reflection);
            redirectAttributes.addFlashAttribute("message", "Reflection deleted.");
        }
        return "redirect:/reflections";
    }
}
