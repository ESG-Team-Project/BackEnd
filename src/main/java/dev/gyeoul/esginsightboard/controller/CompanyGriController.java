package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.service.GriDataItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 회사별 GRI 데이터 관리 컨트롤러
 * <p>
 * 회사별 GRI 데이터를 조회하고 업데이트하는 API를 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "회사별 GRI 데이터", description = "회사별 GRI 데이터 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class CompanyGriController {
    
    private final GriDataItemService griDataItemService;
    
    /**
     * 회사별 모든 GRI 데이터 조회
     *
     * @param companyId 회사 ID
     * @return GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 GRI 데이터 조회", description = "특정 회사의 모든 GRI 데이터 항목을 맵 형태로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터 맵을 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/company/{companyId}/gri")
    public ResponseEntity<Map<String, GriDataItemDto>> getCompanyGriData(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId) {
        Map<String, GriDataItemDto> griData = griDataItemService.getGriDataMapByCompanyId(companyId);
        return ResponseEntity.ok(griData);
    }
    
    /**
     * 회사의 모든 GRI 데이터 일괄 업데이트
     *
     * @param companyId 회사 ID
     * @param griData GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     * @return 업데이트된 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 GRI 데이터 일괄 업데이트", description = "특정 회사의 모든 GRI 데이터 항목을 일괄 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터를 업데이트하고 결과를 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @PutMapping("/company/{companyId}/gri")
    public ResponseEntity<Map<String, GriDataItemDto>> updateCompanyGriData(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId,
            @Parameter(description = "업데이트할 GRI 데이터 맵", required = true) 
            @RequestBody Map<String, GriDataItemDto> griData) {
        Map<String, GriDataItemDto> updatedData = griDataItemService.updateGriDataForCompany(companyId, griData);
        return ResponseEntity.ok(updatedData);
    }
    
    /**
     * 회사의 특정 카테고리 GRI 데이터 업데이트
     *
     * @param companyId 회사 ID
     * @param category 카테고리 (E, S, G)
     * @param griData GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     * @return 업데이트된 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 카테고리 GRI 데이터 업데이트", description = "특정 회사의 특정 카테고리(E, S, G) GRI 데이터 항목을 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터를 업데이트하고 결과를 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @PutMapping("/company/{companyId}/gri/{category}")
    public ResponseEntity<Map<String, GriDataItemDto>> updateCompanyGriDataByCategory(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId,
            @Parameter(description = "카테고리 (E, S, G)", required = true) 
            @PathVariable String category,
            @Parameter(description = "업데이트할 GRI 데이터 맵", required = true) 
            @RequestBody Map<String, GriDataItemDto> griData) {
        Map<String, GriDataItemDto> updatedData = griDataItemService.updateGriDataForCompanyByCategory(companyId, category, griData);
        return ResponseEntity.ok(updatedData);
    }
} 