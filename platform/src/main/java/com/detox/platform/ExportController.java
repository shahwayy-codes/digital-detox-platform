package com.detox.platform;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Lets a user download their own data as CSV files. */
@Controller
public class ExportController {

    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final ReflectionRepository reflectionRepository;

    public ExportController(UserRepository userRepository,
                            CheckInRepository checkInRepository,
                            ReflectionRepository reflectionRepository) {
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.reflectionRepository = reflectionRepository;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/export/checkins.csv")
    public ResponseEntity<byte[]> checkIns(Principal principal) {
        User user = currentUser(principal);
        List<CheckIn> list = new ArrayList<>(checkInRepository.findByParticipation_UserOrderByCheckInDateDesc(user));
        Collections.reverse(list);

        StringBuilder sb = new StringBuilder();
        sb.append(CsvUtil.row(List.of("Date", "Challenge", "Goal met", "Screen time (min)", "Trigger", "Note"))).append("\r\n");
        for (CheckIn c : list) {
            sb.append(CsvUtil.row(List.of(
                    c.getCheckInDate().toString(),
                    c.getParticipation().getChallenge().getTitle(),
                    c.isGoalMet() ? "Yes" : "No",
                    String.valueOf(c.getScreenTimeMinutes()),
                    c.getTriggerTag() == null ? "" : c.getTriggerTag(),
                    c.getNote() == null ? "" : c.getNote()))).append("\r\n");
        }
        return csv(sb.toString(), "detox-checkins.csv");
    }

    @GetMapping("/export/reflections.csv")
    public ResponseEntity<byte[]> reflections(Principal principal) {
        User user = currentUser(principal);
        List<Reflection> list = new ArrayList<>(reflectionRepository.findByUserOrderByCreatedAtDesc(user));
        Collections.reverse(list);

        StringBuilder sb = new StringBuilder();
        sb.append(CsvUtil.row(List.of("Date", "Mood", "Reflection"))).append("\r\n");
        for (Reflection r : list) {
            sb.append(CsvUtil.row(List.of(
                    r.getReflectionDate().toString(),
                    r.getMood(),
                    r.getContent()))).append("\r\n");
        }
        return csv(sb.toString(), "detox-reflections.csv");
    }

    private ResponseEntity<byte[]> csv(String text, String fileName) {
        // A BOM at the start makes Excel open the file as UTF-8.
        byte[] body = ("\uFEFF" + text).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
