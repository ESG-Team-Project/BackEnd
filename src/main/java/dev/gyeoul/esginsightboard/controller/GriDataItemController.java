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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gri")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "GRI 데이터 항목", description = "GRI 프레임워크 기반 ESG 데이터 항목 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class GriDataItemController {

    private final GriDataItemService griDataItemService;

    @Operation(summary = "모든 GRI 데이터 항목 조회", description = "데이터베이스에 저장된 모든 GRI 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<List<GriDataItemDto>> getAllGriDataItems() {
        List<GriDataItemDto> griDataItems = griDataItemService.getAllGriDataItems();
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "ID로 GRI 데이터 항목 조회", description = "특정 ID의 GRI 데이터 항목을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GRI 데이터 항목을 성공적으로 찾았습니다."),
            @ApiResponse(responseCode = "404", description = "해당 ID의 GRI 데이터 항목이 없습니다.", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<GriDataItemDto> getGriDataItemById(
            @Parameter(description = "GRI 데이터 항목 ID", required = true) 
            @PathVariable Long id) {
        return griDataItemService.getGriDataItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "카테고리별 GRI 데이터 항목 조회", description = "Environmental, Social, Governance 카테고리별 GRI 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리와 일치하는 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByCategory(
            @Parameter(description = "GRI 데이터 항목 카테고리 (Environmental, Social, Governance)", required = true, 
                       schema = @Schema(allowableValues = {"Environmental", "Social", "Governance"}))
            @PathVariable String category) {
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByCategory(category);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "GRI 표준 코드별 데이터 항목 조회", description = "특정 GRI 표준 코드(예: GRI 302, GRI 305)에 해당하는 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "GRI 표준 코드와 일치하는 데이터 항목 목록을 반환합니다.")
    @GetMapping("/standard/{standardCode}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByStandardCode(
            @Parameter(description = "GRI 표준 코드 (예: GRI 302, GRI 305)", required = true)
            @PathVariable String standardCode) {
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByStandardCode(standardCode);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "공시 코드별 데이터 항목 조회", description = "특정 공시 코드(예: 302-1, 305-1)에 해당하는 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공시 코드와 일치하는 데이터 항목 목록을 반환합니다.")
    @GetMapping("/disclosure/{disclosureCode}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByDisclosureCode(
            @Parameter(description = "공시 코드 (예: 302-1, 305-1)", required = true)
            @PathVariable String disclosureCode) {
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByDisclosureCode(disclosureCode);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "보고 기간 내 데이터 항목 조회", description = "특정 보고 기간 내의 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "해당 보고 기간 내의 데이터 항목 목록을 반환합니다.")
    @GetMapping("/period")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByReportingPeriod(
            @Parameter(description = "보고 기간 시작일 (형식: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "보고 기간 종료일 (형식: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByReportingPeriod(startDate, endDate);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "검증 상태별 데이터 항목 조회", description = "특정 검증 상태(미검증, 검증중, 검증완료 등)의 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "해당 검증 상태의 데이터 항목 목록을 반환합니다.")
    @GetMapping("/verification/{status}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByVerificationStatus(
            @Parameter(description = "검증 상태 (미검증, 검증중, 검증완료 등)", required = true)
            @PathVariable String status) {
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByVerificationStatus(status);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "GRI 데이터 항목 생성", description = "새로운 GRI 데이터 항목을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "GRI 데이터 항목이 성공적으로 생성되었습니다.")
    @PostMapping
    public ResponseEntity<GriDataItemDto> createGriDataItem(
            @Parameter(description = "생성할 GRI 데이터 항목", required = true, 
                       schema = @Schema(implementation = GriDataItemDto.class))
            @RequestBody GriDataItemDto griDataItemDto) {
        GriDataItemDto createdGriDataItem = griDataItemService.saveGriDataItem(griDataItemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGriDataItem);
    }

    @Operation(summary = "GRI 데이터 항목 수정", description = "기존 GRI 데이터 항목을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GRI 데이터 항목이 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 ID의 GRI 데이터 항목이 없습니다.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<GriDataItemDto> updateGriDataItem(
            @Parameter(description = "수정할 GRI 데이터 항목 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "수정된 GRI 데이터 항목", required = true,
                       schema = @Schema(implementation = GriDataItemDto.class))
            @RequestBody GriDataItemDto griDataItemDto) {
        return griDataItemService.updateGriDataItem(id, griDataItemDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "GRI 데이터 항목 삭제", description = "특정 ID의 GRI 데이터 항목을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "GRI 데이터 항목이 성공적으로 삭제되었습니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGriDataItem(
            @Parameter(description = "삭제할 GRI 데이터 항목 ID", required = true)
            @PathVariable Long id) {
        griDataItemService.deleteGriDataItem(id);
        return ResponseEntity.noContent().build();
    }
} 