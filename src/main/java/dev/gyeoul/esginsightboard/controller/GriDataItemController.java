package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.GriDataSearchCriteria;
import dev.gyeoul.esginsightboard.dto.PageResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;

/**
 * GRI 데이터 항목 관리 API 컨트롤러
 * <p>
 * 이 컨트롤러는 GRI 프레임워크 기반 ESG 데이터 항목에 대한 CRUD 작업을 제공합니다.
 * 다양한 조건으로 GRI 데이터를 검색하고 필터링할 수 있습니다.
 * </p>
 */
@Slf4j
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
        log.debug("모든 GRI 데이터 항목 조회 요청을 처리합니다.");
        List<GriDataItemDto> griDataItems = griDataItemService.getAllGriDataItems();
        return ResponseEntity.ok(griDataItems);
    }
    
    @Operation(summary = "페이지네이션된 GRI 데이터 항목 조회", 
              description = "페이지 단위로 GRI 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "페이지네이션이 적용된 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping("/paged")
    public ResponseEntity<PageResponse<GriDataItemDto>> getPaginatedGriDataItems(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (형식: 속성,정렬방향) 예: disclosureCode,asc", example = "disclosureCode,asc") 
            @RequestParam(required = false) String sort) {
        
        log.debug("페이지네이션 GRI 데이터 조회 요청: 페이지={}, 크기={}, 정렬={}", page, size, sort);
        
        // 정렬 설정 처리
        Sort sortObj = Sort.by("id");
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String property = parts[0];
            
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
                sortObj = Sort.by(Sort.Direction.DESC, property);
            } else {
                sortObj = Sort.by(Sort.Direction.ASC, property);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        PageResponse<GriDataItemDto> result = griDataItemService.getPaginatedGriDataItems(pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "GRI 데이터 항목 필터링", 
               description = "다양한 조건으로 GRI 데이터 항목을 필터링합니다.")
    @ApiResponse(responseCode = "200", description = "필터링이 적용된 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<GriDataItemDto>> filterGriDataItems(
            @Parameter(description = "카테고리 (Environmental, Social, Governance, Economic, 일반)") 
            @RequestParam(required = false) String category,
            @Parameter(description = "GRI 표준 코드 (예: GRI 302)") 
            @RequestParam(required = false) String standardCode,
            @Parameter(description = "GRI 공시 코드 (예: 302-1)") 
            @RequestParam(required = false) String disclosureCode,
            @Parameter(description = "보고 기간 시작일", example = "2023-01-01") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @Parameter(description = "보고 기간 종료일", example = "2023-12-31") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @Parameter(description = "검증 상태 (예: 검증완료, 검증중, 미검증)") 
            @RequestParam(required = false) String verificationStatus,
            @Parameter(description = "회사 ID") 
            @RequestParam(required = false) Long companyId,
            @Parameter(description = "키워드 검색 (제목, 설명에서 검색)") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (형식: 속성,정렬방향)", example = "disclosureCode,asc") 
            @RequestParam(required = false) String sort) {
        
        log.debug("GRI 데이터 필터링 요청: 카테고리={}, 표준코드={}, 회사ID={}", category, standardCode, companyId);
        
        // 검색 조건 객체 생성
        GriDataSearchCriteria criteria = GriDataSearchCriteria.builder()
                .category(category)
                .standardCode(standardCode)
                .disclosureCode(disclosureCode)
                .reportingPeriodStart(periodStart)
                .reportingPeriodEnd(periodEnd)
                .verificationStatus(verificationStatus)
                .companyId(companyId)
                .keyword(keyword)
                .sort(sort)
                .build();
        
        // 정렬 설정 처리
        Sort sortObj = Sort.by("id");
        if (StringUtils.hasText(sort)) {
            String[] parts = sort.split(",");
            String property = parts[0];
            
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
                sortObj = Sort.by(Sort.Direction.DESC, property);
            } else {
                sortObj = Sort.by(Sort.Direction.ASC, property);
            }
        }
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        PageResponse<GriDataItemDto> result = griDataItemService.findByCriteria(criteria, pageable);
        
        return ResponseEntity.ok(result);
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
        log.debug("ID가 {}인 GRI 데이터 항목 조회 요청을 처리합니다.", id);
        return griDataItemService.getGriDataItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "특정 회사의 GRI 데이터 항목 맵 조회", 
               description = "특정 회사의 모든 GRI 데이터 항목을 Map 형태로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회사의 GRI 데이터 항목 맵을 반환합니다.")
    @GetMapping("/company/{companyId}/map")
    public ResponseEntity<Map<String, GriDataItemDto>> getGriDataMapByCompany(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId) {
        log.debug("회사 ID {}의 GRI 데이터 항목 맵 조회 요청을 처리합니다.", companyId);
        Map<String, GriDataItemDto> griDataMap = griDataItemService.getGriDataMapByCompanyId(companyId);
        return ResponseEntity.ok(griDataMap);
    }

    @Operation(summary = "특정 회사의 GRI 데이터 항목 맵 업데이트", 
               description = "특정 회사의 GRI 데이터 항목들을 Map 형태로 일괄 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "회사의 GRI 데이터 항목이 성공적으로 업데이트되었습니다.")
    @PutMapping("/company/{companyId}/map")
    public ResponseEntity<Map<String, GriDataItemDto>> updateGriDataMapForCompany(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId,
            @RequestBody Map<String, GriDataItemDto> griDataMap) {
        log.debug("회사 ID {}의 GRI 데이터 항목 맵 업데이트 요청을 처리합니다. 항목 수: {}", companyId, griDataMap.size());
        Map<String, GriDataItemDto> updatedMap = griDataItemService.updateGriDataForCompany(companyId, griDataMap);
        return ResponseEntity.ok(updatedMap);
    }

    @Operation(summary = "카테고리별 GRI 데이터 항목 조회", description = "Environmental, Social, Governance 카테고리별 GRI 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리와 일치하는 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByCategory(
            @Parameter(description = "GRI 데이터 항목 카테고리 (Environmental, Social, Governance)", required = true, 
                       schema = @Schema(allowableValues = {"Environmental", "Social", "Governance"}))
            @PathVariable String category) {
        log.debug("카테고리 {}에 속하는 GRI 데이터 항목 조회 요청을 처리합니다.", category);
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByCategory(category);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "GRI 표준 코드별 데이터 항목 조회", description = "특정 GRI 표준 코드(예: GRI 302, GRI 305)에 해당하는 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "GRI 표준 코드와 일치하는 데이터 항목 목록을 반환합니다.")
    @GetMapping("/standard/{standardCode}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByStandardCode(
            @Parameter(description = "GRI 표준 코드 (예: GRI 302, GRI 305)", required = true)
            @PathVariable String standardCode) {
        log.debug("표준 코드 {}에 속하는 GRI 데이터 항목 조회 요청을 처리합니다.", standardCode);
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByStandardCode(standardCode);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "공시 코드별 데이터 항목 조회", description = "특정 공시 코드(예: 302-1, 305-1)에 해당하는 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공시 코드와 일치하는 데이터 항목 목록을 반환합니다.")
    @GetMapping("/disclosure/{disclosureCode}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByDisclosureCode(
            @Parameter(description = "공시 코드 (예: 302-1, 305-1)", required = true)
            @PathVariable String disclosureCode) {
        log.debug("공시 코드 {}에 속하는 GRI 데이터 항목 조회 요청을 처리합니다.", disclosureCode);
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
        log.debug("보고 기간 {} ~ {} 내의 GRI 데이터 항목 조회 요청을 처리합니다.", startDate, endDate);
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByReportingPeriod(startDate, endDate);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "검증 상태별 데이터 항목 조회", description = "특정 검증 상태(미검증, 검증중, 검증완료 등)의 데이터 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "해당 검증 상태의 데이터 항목 목록을 반환합니다.")
    @GetMapping("/verification/{status}")
    public ResponseEntity<List<GriDataItemDto>> getGriDataItemsByVerificationStatus(
            @Parameter(description = "검증 상태 (미검증, 검증중, 검증완료 등)", required = true)
            @PathVariable String status) {
        log.debug("검증 상태가 {}인 GRI 데이터 항목 조회 요청을 처리합니다.", status);
        List<GriDataItemDto> griDataItems = griDataItemService.getGriDataItemsByVerificationStatus(status);
        return ResponseEntity.ok(griDataItems);
    }

    @Operation(summary = "GRI 데이터 항목 생성", description = "새로운 GRI 데이터 항목을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "GRI 데이터 항목이 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GriDataItemDto> createGriDataItem(
            @Parameter(description = "생성할 GRI 데이터 항목", required = true)
            @RequestBody GriDataItemDto griDataItemDto) {
        log.debug("새로운 GRI 데이터 항목 생성 요청을 처리합니다.");
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
        log.debug("ID가 {}인 GRI 데이터 항목 수정 요청을 처리합니다.", id);
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
        log.debug("ID가 {}인 GRI 데이터 항목 삭제 요청을 처리합니다.", id);
        griDataItemService.deleteGriDataItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "GRI 데이터 항목 일괄 저장", description = "여러 GRI 데이터 항목을 한 번에 저장합니다.")
    @ApiResponse(responseCode = "201", description = "GRI 데이터 항목들이 성공적으로 저장되었습니다.")
    @PostMapping("/batch")
    public ResponseEntity<List<GriDataItemDto>> batchCreateGriDataItems(
            @Parameter(description = "저장할 GRI 데이터 항목 목록", required = true)
            @RequestBody List<GriDataItemDto> griDataItems,
            @Parameter(description = "회사 ID", required = true)
            @RequestParam Long companyId) {
        log.debug("GRI 데이터 항목 일괄 저장 요청을 처리합니다. 항목 수: {}, 회사 ID: {}", 
                 griDataItems.size(), companyId);
        List<GriDataItemDto> createdItems = griDataItemService.saveAllGriDataItems(griDataItems, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItems);
    }

    /**
     * 회사별 GRI 데이터 배치 저장 (프론트엔드 호환용)
     */
    @Operation(summary = "회사별 GRI 데이터 배치 저장", 
               description = "특정 회사의 여러 GRI 데이터 항목을 일괄 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "회사를 찾을 수 없음")
    })
    @PostMapping("/company/{companyId}/batch")
    public ResponseEntity<List<GriDataItemDto>> batchSaveGriData(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId,
            @Parameter(description = "저장할 GRI 데이터 항목 목록", required = true) 
            @RequestBody List<GriDataItemDto> griDataItems) {
        
        log.debug("회사 ID {}의 GRI 데이터 일괄 저장 요청. 항목 수: {}", companyId, griDataItems.size());
        
        // 각 항목에 회사 ID 설정 (누락된 경우)
        griDataItems.forEach(item -> {
            if (item.getCompanyId() == null) {
                item.setCompanyId(companyId);
            }
        });
        
        // 데이터 유효성 검사
        List<GriDataItemDto> validItems = griDataItems.stream()
            .filter(item -> item.getStandardCode() != null && !item.getStandardCode().isEmpty() && 
                           item.getDisclosureCode() != null && !item.getDisclosureCode().isEmpty())
            .collect(Collectors.toList());
        
        if (validItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        // 데이터 저장
        List<GriDataItemDto> savedItems = new ArrayList<>();
        for (GriDataItemDto item : validItems) {
            try {
                GriDataItemDto savedItem = griDataItemService.saveGriDataItem(item);
                savedItems.add(savedItem);
            } catch (Exception e) {
                log.error("GRI 데이터 저장 오류: {}", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(savedItems);
    }
    
    /**
     * 단일 GRI 카테고리 데이터 저장
     */
    @Operation(summary = "단일 GRI 카테고리 데이터 저장", 
               description = "특정 회사의, 특정 GRI 카테고리 데이터를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "회사를 찾을 수 없음")
    })
    @PostMapping("/company/{companyId}/category/{categoryId}")
    public ResponseEntity<GriDataItemDto> saveSingleCategory(
            @Parameter(description = "회사 ID", required = true) 
            @PathVariable Long companyId,
            @Parameter(description = "카테고리 ID (예: GRI-302-1)", required = true) 
            @PathVariable String categoryId,
            @Parameter(description = "저장할 GRI 데이터", required = true) 
            @RequestBody GriDataItemDto categoryData) {
        
        log.debug("회사 ID {}의 카테고리 {} 데이터 저장 요청", companyId, categoryId);
        
        // 카테고리 ID 형식 검증 및 파싱
        String[] parts = categoryId.split("-");
        if (parts.length < 2) {
            log.warn("잘못된 카테고리 ID 형식: {}", categoryId);
            return ResponseEntity.badRequest().build();
        }
        
        // 카테고리 데이터 설정
        categoryData.setCompanyId(companyId);
        categoryData.setStandardCode(parts[0]);
        categoryData.setDisclosureCode(parts[1]);
        
        // 기존 데이터 조회 (업데이트인 경우)
        Optional<GriDataItemDto> existingData = griDataItemService
            .findByCompanyIdAndStandardCodeAndDisclosureCode(companyId, parts[0], parts[1]);
        
        if (existingData.isPresent()) {
            categoryData.setId(existingData.get().getId());
        }
        
        // 데이터 저장
        try {
            GriDataItemDto savedData = griDataItemService.saveGriDataItem(categoryData);
            return ResponseEntity.ok(savedData);
        } catch (Exception e) {
            log.error("카테고리 {} 데이터 저장 오류: {}", categoryId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 