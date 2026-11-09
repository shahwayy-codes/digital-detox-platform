package com.detox.platform;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TipController {

    private final TipRepository tipRepository;

    public TipController(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }

    @GetMapping("/tips")
    public String list(@RequestParam(required = false) String type, Model model) {
        String selected = type == null ? "" : type.trim();
        List<Tip> tips = selected.isEmpty()
                ? tipRepository.findAllByOrderByCreatedAtDesc()
                : tipRepository.findByTipTypeOrderByCreatedAtDesc(selected);
        model.addAttribute("tips", tips);
        model.addAttribute("selectedType", selected);
        return "tips";
    }
}
