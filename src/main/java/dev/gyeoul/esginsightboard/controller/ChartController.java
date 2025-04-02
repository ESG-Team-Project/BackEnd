package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.EsgChartDataDto;
import dev.gyeoul.esginsightboard.service.ChartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charts") // 기본 URL: /api/charts
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    // 차트 데이터 저장 API
    @PostMapping
    public ResponseEntity<String> saveChart(@Valid @RequestBody EsgChartDataDto dto) {
        chartService.saveChartData(dto); // 서비스 호출
        return ResponseEntity.ok("차트 데이터가 성공적으로 저장되었습니다.");
    }
}
