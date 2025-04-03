package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.EsgChartDataDto;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.ChartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charts") // 기본 URL: /api/charts
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    /**
     * ESG 차트 데이터를 저장하는 POST API
     * URL: /api/charts
     *
     * @param chartDto - 클라이언트에서 전송된 차트 데이터
     * @param request - 인증 정보를 담고 있는 HttpServletRequest 객체
     * @return ResponseEntity<String> - 저장 성공 또는 실패 메시지 반환
     */
    @PostMapping
    public ResponseEntity<String> saveChart(
            // JSON 요청 본문울 DTO로 매핑하고 유효성 검사 수행
            @Valid @RequestBody EsgChartDataDto chartDto,
            HttpServletRequest request) {

        // 인증 필터 또는 인터셉터에서 저장한 사용자 정보를 request에서 추출
        UserDto user = (UserDto) request.getAttribute("user");

        // 인증되지 않은 사용자일 경우, 401 Unauthorized 응답 반환
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자가 아닙니다.");
        }

        // 인증된 사용자와 차트 데이터를 서비스에 전달하여 저장 처리
        chartService.saveChartData(chartDto, user);

        // 저장 성공 시 200 ok 응답 반환
        return ResponseEntity.ok("차트 데이터가 성공적으로 저장되었습니다.");
    }

//    @GetMapping("/{companyId}/{indicatorCode}")
//    public ResponseEntity<List<EsgInputValueDto>> getChartData(
//            @PathVariable Long companyId,
//            @PathVariable String indicatorCode) {
//        return ResponseEntity.ok(chartService.getChartData(companyId, indicatorCode));
//    }
}
