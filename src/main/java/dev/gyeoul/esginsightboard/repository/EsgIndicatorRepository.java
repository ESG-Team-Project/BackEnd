package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.EsgIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EsgIndicatorRepository extends JpaRepository<EsgIndicator, Long> {

    // 지표 제목으로 조회 (예: "원재료 사용량")
    Optional<EsgIndicator> findByTitle(String title);
}
