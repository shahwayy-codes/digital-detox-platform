package com.detox.platform;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private FriendGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime joinedAt = LocalDateTime.now();

    public GroupMember() {
    }

    public GroupMember(FriendGroup group, User user) {
        this.group = group;
        this.user = user;
    }

    public Long getId() { return id; }
    public FriendGroup getGroup() { return group; }
    public User getUser() { return user; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
