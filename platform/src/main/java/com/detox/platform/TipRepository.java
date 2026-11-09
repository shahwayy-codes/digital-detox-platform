package com.detox.platform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipRepository extends JpaRepository<Tip, Long> {

    List<Tip> findAllByOrderByCreatedAtDesc();

    List<Tip> findByTipTypeOrderByCreatedAtDesc(String tipType);
}
