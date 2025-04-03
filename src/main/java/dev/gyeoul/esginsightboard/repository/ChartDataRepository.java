package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.ChartData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChartDataRepository extends JpaRepository<ChartData, Long> {

    // ✅ 특정 필드로 조회하는 메서드
    List<ChartData> findByTitle(String title); // 제목으로 조회

    List<ChartData> findByDescriptionContaining(String keyword); // 설명에 특정 키워드 포함된 데이터 조회

    List<ChartData> findByCategory(String category); // ESG 카테고리(E, S, G)별 조회

    List<ChartData> findByIndicator(String indicator); // 특정 세부 지표(indicator)별 조회

    List<ChartData> findByChartGrid(Integer chartGrid); // 차트 칸 개수로 조회

    List<ChartData> findByDataContaining(String data); // JSON 데이터 내 특정 값 포함 조회 (추가)
}