package com.detox.platform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByChallengeOrderByCreatedAtDesc(Challenge challenge);

    List<Comment> findByReportedTrueOrderByCreatedAtDesc();
}
