package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.EsgIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EsgIndicatorRepository extends JpaRepository<EsgIndicator, Long> {

    // 지표 코드 조회 (예: "301-1")
    Optional<EsgIndicator> findByIndicatorCode(String indicatorCode);
}
