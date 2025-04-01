package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.EsgInputValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsgInputValueRepository extends JpaRepository<EsgInputValue, Long> {
    // 기본 CRUD 기능만 사용 시 추가 메서드 없이도 충분합니다
}
