package com.detox.platform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByUserAndChallenge(User user, Challenge challenge);

    List<Participation> findByUser(User user);

    long countByChallenge(Challenge challenge);
}
