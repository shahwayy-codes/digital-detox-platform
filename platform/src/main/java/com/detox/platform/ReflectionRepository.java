package com.detox.platform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    List<Reflection> findByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);
}
