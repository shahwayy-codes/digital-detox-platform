package com.detox.platform;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "focus_sessions")
public class FocusSession {

    public static final String RUNNING = "RUNNING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int plannedMinutes;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private String status = RUNNING;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getPlannedMinutes() { return plannedMinutes; }
    public void setPlannedMinutes(int plannedMinutes) { this.plannedMinutes = plannedMinutes; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getStartedAtText() { return startedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
