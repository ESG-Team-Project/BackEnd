package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.ChartDataDto;
import dev.gyeoul.esginsightboard.service.ChartDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartDataController {

    private final ChartDataService chartDataService;

    // ✅ 차트 데이터 저장
    @PostMapping
    public ResponseEntity<ChartDataDto> saveChart(@RequestBody ChartDataDto dto) {
        ChartDataDto savedChart = chartDataService.saveChartData(dto);
        return ResponseEntity.ok(savedChart);
    }

    // ✅ 특정 ID 리스트 또는 전체 차트 조회
    @GetMapping
    public ResponseEntity<List<ChartDataDto>> getCharts(@RequestParam(required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(chartDataService.getAllChartData()); // 모든 차트 조회
        } else {
            return ResponseEntity.ok(chartDataService.getChartsByIds(ids)); // 특정 ID 차트 조회
        }
    }

    // ✅ 제목으로 차트 조회
    @GetMapping("/title/{title}")
    public ResponseEntity<List<ChartDataDto>> getChartsByTitle(@PathVariable String title) {
        return ResponseEntity.ok(chartDataService.getChartDataByTitle(title));
    }

    // ✅ 설명에 특정 키워드 포함된 차트 조회
    @GetMapping("/description/{keyword}")
    public ResponseEntity<List<ChartDataDto>> getChartsByDescription(@PathVariable String keyword) {
        return ResponseEntity.ok(chartDataService.getChartDataByDescription(keyword));
    }

    // ✅ ESG 카테고리별 차트 조회 (E, S, G)
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ChartDataDto>> getChartsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(chartDataService.getChartDataByCategory(category));
    }

    // ✅ 특정 세부 지표(indicator)별 차트 조회
    @GetMapping("/indicator/{indicator}")
    public ResponseEntity<List<ChartDataDto>> getChartsByIndicator(@PathVariable String indicator) {
        return ResponseEntity.ok(chartDataService.getChartDataByIndicator(indicator));
    }

    // ✅ 특정 차트 칸 개수로 조회
    @GetMapping("/grid/{chartGrid}")
    public ResponseEntity<List<ChartDataDto>> getChartsByChartGrid(@PathVariable Integer chartGrid) {
        return ResponseEntity.ok(chartDataService.getChartDataByChartGrid(chartGrid));
    }

    // ✅ 차트 데이터 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChart(@PathVariable Long id) {
        chartDataService.deleteChartData(id);
        return ResponseEntity.noContent().build();
    }
}