package com.detox.platform;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckInController {

    private final CheckInRepository checkInRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;

    public CheckInController(CheckInRepository checkInRepository,
                             ParticipationRepository participationRepository,
                             UserRepository userRepository) {
        this.checkInRepository = checkInRepository;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/checkin")
    public String checkInPage(Principal principal, Model model) {
        User user = currentUser(principal);
        LocalDate today = LocalDate.now();
        List<Participation> participations = participationRepository.findByUser(user);

        Map<Long, CheckIn> todayMap = new HashMap<>();
        for (Participation p : participations) {
            CheckIn existing = checkInRepository.findByParticipationAndCheckInDate(p, today).orElse(null);
            if (existing != null) {
                todayMap.put(p.getId(), existing);
            }
        }

        model.addAttribute("participations", participations);
        model.addAttribute("todayMap", todayMap);
        model.addAttribute("today", today.toString());
        return "checkin";
    }

    @PostMapping("/checkin/{participationId}")
    public String submit(@PathVariable Long participationId,
                         @RequestParam boolean goalMet,
                         @RequestParam(required = false) Integer screenTimeMinutes,
                         @RequestParam(required = false) String note,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {

        User user = currentUser(principal);
        Participation participation = participationRepository.findById(participationId).orElse(null);
        if (participation == null || !participation.getUser().getId().equals(user.getId())) {
            return "redirect:/checkin";
        }

        if (screenTimeMinutes == null || screenTimeMinutes < 0 || screenTimeMinutes > 1440) {
            redirectAttributes.addFlashAttribute("error", "Enter screen time between 0 and 1440 minutes.");
            return "redirect:/checkin";
        }

        LocalDate today = LocalDate.now();
        if (checkInRepository.existsByParticipationAndCheckInDate(participation, today)) {
            redirectAttributes.addFlashAttribute("error", "You already checked in today for this challenge.");
            return "redirect:/checkin";
        }

        String cleanNote = note == null ? "" : note.trim();
        if (cleanNote.length() > 300) {
            cleanNote = cleanNote.substring(0, 300);
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setParticipation(participation);
        checkIn.setCheckInDate(today);
        checkIn.setGoalMet(goalMet);
        checkIn.setScreenTimeMinutes(screenTimeMinutes);
        checkIn.setNote(cleanNote);
        checkInRepository.save(checkIn);

        redirectAttributes.addFlashAttribute("message", "Check-in saved. Nice work!");
        return "redirect:/checkin";
    }

    @GetMapping("/history")
    public String history(Principal principal, Model model) {
        User user = currentUser(principal);
        List<CheckIn> checkIns = checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user);

        long avgMinutes = Math.round(checkIns.stream()
                .mapToInt(CheckIn::getScreenTimeMinutes)
                .average()
                .orElse(0));

        model.addAttribute("checkIns", checkIns);
        model.addAttribute("total", checkIns.size());
        model.addAttribute("avgMinutes", avgMinutes);
        return "history";
    }
}
