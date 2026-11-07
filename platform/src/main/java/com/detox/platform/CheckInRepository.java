package com.detox.platform;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByParticipationAndCheckInDate(Participation participation, LocalDate checkInDate);

    boolean existsByParticipationAndCheckInDate(Participation participation, LocalDate checkInDate);

    List<CheckIn> findByParticipation_UserOrderByCheckInDateDesc(User user);
}
