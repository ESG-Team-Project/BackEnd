package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.DashboardDto;
import dev.gyeoul.esginsightboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "대시보드", description = "ESG 대시보드 정보 API")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(
        summary = "대시보드 종합 정보 조회", 
        description = "ESG 데이터 대시보드에 필요한 종합 정보를 조회합니다. 카테고리별 점수, 항목 수, 주요 지표 등을 포함합니다. JWT 토큰이 필요합니다."
    )
    @ApiResponse(
        responseCode = "200", 
        description = "성공적으로 대시보드 정보를 조회했습니다.",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardDto.class))
    )
    public ResponseEntity<DashboardDto> getDashboardInfo() {
        DashboardDto dashboardInfo = dashboardService.getDashboardInfo();
        return ResponseEntity.ok(dashboardInfo);
    }
} 