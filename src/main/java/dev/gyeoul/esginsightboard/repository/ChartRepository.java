package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.dto.EsgChartDataDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ChartRepository {

    // 임시 저장소 (DB 대체용)
    private final List<EsgChartDataDto> chartDataStore = new ArrayList<>();

    // 차트 데이터 저장
    public void save(EsgChartDataDto dto) {
        chartDataStore.add(dto);
    }

    // 전체 저장 데이터 반환 (옵션)
    public List<EsgChartDataDto> findAll() {
        return chartDataStore;
    }
}
