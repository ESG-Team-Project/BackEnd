package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.EsgCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EsgCategoryRepository extends JpaRepository<EsgCategory, Long> {
    Optional<EsgCategory> findByCode(String code); // "E", "S", "G"로 카테고리 조회
}
