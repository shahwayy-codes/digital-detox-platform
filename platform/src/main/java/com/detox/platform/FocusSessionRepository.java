package com.detox.platform;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findFirstByUserAndStatusOrderByStartedAtDesc(User user, String status);

    List<FocusSession> findByUserAndStatus(User user, String status);

    List<FocusSession> findTop10ByUserOrderByStartedAtDesc(User user);
}
