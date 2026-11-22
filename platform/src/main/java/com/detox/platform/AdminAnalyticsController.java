package com.detox.platform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Admin only: the /admin/** rule in SecurityConfig already protects this page. */
@Controller
public class AdminAnalyticsController {

    private static final int DAYS = 14;

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    public AdminAnalyticsController(CheckInRepository checkInRepository, UserRepository userRepository) {
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        List<AnalyticsCalc.Entry> entries = new ArrayList<>();
        for (CheckIn c : checkInRepository.findAll()) {
            entries.add(new AnalyticsCalc.Entry(c.getCheckInDate(),
                    c.getParticipation().getUser().getId(), c.getScreenTimeMinutes()));
        }
        List<AnalyticsCalc.DayPoint> points = AnalyticsCalc.daily(entries, LocalDate.now(), DAYS);
        int peak = points.stream().mapToInt(AnalyticsCalc.DayPoint::getActiveUsers).max().orElse(0);
        int overall = entries.isEmpty() ? 0
                : Math.round((float) entries.stream().mapToInt(AnalyticsCalc.Entry::getMinutes).sum() / entries.size());

        model.addAttribute("points", points);
        model.addAttribute("days", DAYS);
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("checkInCount", entries.size());
        model.addAttribute("peakUsers", peak);
        model.addAttribute("overallAvg", overall);
        return "admin-analytics";
    }
}
