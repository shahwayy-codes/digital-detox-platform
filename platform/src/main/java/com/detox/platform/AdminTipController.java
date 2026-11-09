package com.detox.platform;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminTipController {

    private static final List<String> TYPES = List.of("Tip", "Offline idea");

    private final TipRepository tipRepository;

    public AdminTipController(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }

    @GetMapping("/admin/tips")
    public String list(Model model) {
        model.addAttribute("tips", tipRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("types", TYPES);
        return "admin-tips";
    }

    @PostMapping("/admin/tips")
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam String tipType,
                         RedirectAttributes redirectAttributes) {
        String cleanTitle = title.trim();
        String cleanContent = content.trim();
        if (cleanTitle.length() < 3 || cleanTitle.length() > 100) {
            redirectAttributes.addFlashAttribute("error", "Title must be 3 to 100 characters.");
        } else if (cleanContent.length() < 10 || cleanContent.length() > 600) {
            redirectAttributes.addFlashAttribute("error", "Text must be 10 to 600 characters.");
        } else if (!TYPES.contains(tipType)) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid type.");
        } else {
            Tip tip = new Tip();
            tip.setTitle(cleanTitle);
            tip.setContent(cleanContent);
            tip.setTipType(tipType);
            tipRepository.save(tip);
            redirectAttributes.addFlashAttribute("message", "Added.");
        }
        return "redirect:/admin/tips";
    }

    @PostMapping("/admin/tips/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (tipRepository.existsById(id)) {
            tipRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Deleted.");
        }
        return "redirect:/admin/tips";
    }
}
