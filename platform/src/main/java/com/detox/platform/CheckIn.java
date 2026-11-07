package com.detox.platform;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participation_id", "check_in_date"}))
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participation_id")
    private Participation participation;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "goal_met", nullable = false)
    private boolean goalMet;

    @Column(name = "screen_time_minutes", nullable = false)
    private int screenTimeMinutes;

    @Column(length = 300)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Participation getParticipation() { return participation; }
    public void setParticipation(Participation participation) { this.participation = participation; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public boolean isGoalMet() { return goalMet; }
    public void setGoalMet(boolean goalMet) { this.goalMet = goalMet; }
    public int getScreenTimeMinutes() { return screenTimeMinutes; }
    public void setScreenTimeMinutes(int screenTimeMinutes) { this.screenTimeMinutes = screenTimeMinutes; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
