package com.detox.platform;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupAndUser(FriendGroup group, User user);

    Optional<GroupMember> findByGroupAndUser(FriendGroup group, User user);

    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroup(FriendGroup group);

    long countByGroup(FriendGroup group);
}
