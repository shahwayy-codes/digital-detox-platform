package com.detox.platform;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tips")
public class Tip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 600)
    private String content;

    @Column(name = "tip_type", nullable = false)
    private String tipType;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTipType() { return tipType; }
    public void setTipType(String tipType) { this.tipType = tipType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
