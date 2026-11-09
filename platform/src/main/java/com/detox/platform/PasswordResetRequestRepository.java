package com.detox.platform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findByHandledFalseOrderByCreatedAtDesc();

    boolean existsByEmailAndHandledFalse(String email);
}
