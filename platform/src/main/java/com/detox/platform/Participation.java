package com.detox.platform;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"}))
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "ACTIVE";

    public Participation() {
    }

    public Participation(User user, Challenge challenge) {
        this.user = user;
        this.challenge = challenge;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Challenge getChallenge() { return challenge; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
