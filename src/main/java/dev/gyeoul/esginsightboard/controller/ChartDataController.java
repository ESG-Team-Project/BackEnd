package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.ChartDataDto;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.service.ChartDataService;
import dev.gyeoul.esginsightboard.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 차트 데이터 관리 관련 API 컨트롤러
 * <p>
 * 이 컨트롤러는 차트 데이터의 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 사용자별 차트 데이터 관리 및 다양한 조건으로 차트 조회가 가능합니다.
 * </p>
 */
@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
@Tag(name = "차트 데이터 API", description = "차트 데이터 생성, 조회, 수정, 삭제 기능 제공")
@SecurityRequirement(name = "bearerAuth")
public class ChartDataController {

    private final ChartDataService chartDataService;
    private final UserRepository userRepository; // ✅ User 객체 조회를 위한 서비스 추가

    // ✅ 현재 로그인한 사용자의 차트 데이터 저장
    @PostMapping
    @Operation(summary = "차트 데이터 저장", description = "로그인한 사용자의 차트 데이터를 저장합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "차트 데이터 저장 성공", 
                     content = @Content(schema = @Schema(implementation = ChartDataDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    public ResponseEntity<ChartDataDto> saveChart(
            HttpServletRequest request, 
            @Parameter(description = "저장할 차트 데이터", required = true) 
            @Valid @RequestBody ChartDataDto dto) {
        UserDto userDto = (UserDto) request.getAttribute("user");
        if (userDto == null) {
            return ResponseEntity.status(401).build();
        }

        ChartDataDto savedChart = chartDataService.saveChartData(dto, userDto); // ✅ User 객체 전달
        return ResponseEntity.ok(savedChart);
    }

    // ✅ 특정 ID 리스트 또는 전체 차트 조회
    @GetMapping
    @Operation(summary = "차트 데이터 조회", description = "특정 ID 리스트를 기준으로 차트를 조회하거나, ID가 제공되지 않으면 모든 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getCharts(
            @Parameter(description = "조회할 차트 ID 목록 (생략 가능)")
            @RequestParam(required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(chartDataService.getAllChartData()); // 모든 차트 조회
        } else {
            return ResponseEntity.ok(chartDataService.getChartsByIds(ids)); // 특정 ID 차트 조회
        }
    }

    // ✅ 제목으로 차트 조회
    @GetMapping("/title/{title}")
    @Operation(summary = "제목으로 차트 조회", description = "제공된: 제목과: 일치하는 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getChartsByTitle(
            @Parameter(description = "검색할 차트 제목", required = true)
            @PathVariable String title) {
        return ResponseEntity.ok(chartDataService.getChartDataByTitle(title));
    }

    // ✅ 설명에 특정 키워드 포함된 차트 조회
    @GetMapping("/description/{keyword}")
    @Operation(summary = "설명으로 차트 조회", description = "차트 설명에 특정 키워드가 포함된 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getChartsByDescription(
            @Parameter(description = "검색할 키워드", required = true)
            @PathVariable String keyword) {
        return ResponseEntity.ok(chartDataService.getChartDataByDescription(keyword));
    }

    // ✅ ESG 카테고리별 차트 조회 (E, S, G)
    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 차트 조회", description = "ESG 카테고리(E, S, G)별로 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getChartsByCategory(
            @Parameter(description = "ESG 카테고리 (E, S, G)", required = true,
                       schema = @Schema(allowableValues = {"E", "S", "G"}))
            @PathVariable String category) {
        return ResponseEntity.ok(chartDataService.getChartDataByCategory(category));
    }

    // ✅ 특정 세부 지표(indicator)별 차트 조회
    @GetMapping("/indicator/{indicator}")
    @Operation(summary = "지표별 차트 조회", description = "특정 세부 지표(indicator)별로 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getChartsByIndicator(
            @Parameter(description = "검색할 세부 지표명", required = true)
            @PathVariable String indicator) {
        return ResponseEntity.ok(chartDataService.getChartDataByIndicator(indicator));
    }

    // ✅ 특정 차트 칸 개수로 조회
    @GetMapping("/grid/{chartGrid}")
    @Operation(summary = "그리드 크기별 차트 조회", description = "특정 차트 그리드 크기에 맞는 차트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공")
    public ResponseEntity<List<ChartDataDto>> getChartsByChartGrid(
            @Parameter(description = "차트 그리드 크기", required = true)
            @PathVariable Integer chartGrid) {
        return ResponseEntity.ok(chartDataService.getChartDataByChartGrid(chartGrid));
    }

    // ✅ 차트 데이터 업데이트
    @PutMapping("/{id}")
    @Operation(summary = "차트 데이터 수정", description = "특정 ID의 차트 데이터를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "차트 데이터 수정 성공",
                     content = @Content(schema = @Schema(implementation = ChartDataDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
        @ApiResponse(responseCode = "404", description = "차트를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<ChartDataDto> updateChart(
            HttpServletRequest request, 
            @Parameter(description = "수정할 차트 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "수정된 차트 데이터", required = true)
            @RequestBody ChartDataDto dto) {
        UserDto userDto = (UserDto) request.getAttribute("user");
        if (userDto == null) {
            return ResponseEntity.status(401).build();
        }

        // ✅ UserRepository를 직접 사용해 User 조회
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userDto.getId()));

        ChartDataDto updatedChart = chartDataService.updateChartData(id, dto, userDto); // ✅ User 객체 전달
        return ResponseEntity.ok(updatedChart);
    }

    // ✅ 차트 데이터 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "차트 데이터 삭제", description = "특정 ID의 차트 데이터를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "차트 데이터 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "차트를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> deleteChart(
            @Parameter(description = "삭제할 차트 ID", required = true)
            @PathVariable Long id) {
        chartDataService.deleteChartData(id);
        return ResponseEntity.noContent().build();
    }
}