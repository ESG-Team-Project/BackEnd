package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.GriDataSearchCriteria;
import dev.gyeoul.esginsightboard.dto.PageResponse;
import dev.gyeoul.esginsightboard.dto.UserDto;
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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
@Slf4j
public class CompanyGriController {
    
    private final GriDataItemService griDataItemService;
    
    /**
     * 현재 사용자 회사의 모든 GRI 데이터 조회
     *
     * @return GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 GRI 데이터 조회", description = "현재 로그인한 사용자 회사의 모든 GRI 데이터 항목을 맵 형태로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터 맵을 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/company/gri")
    public ResponseEntity<Map<String, GriDataItemDto>> getCompanyGriData(HttpServletRequest request) {
        log.info("=== 회사 GRI 데이터 조회 API 호출 - 요청 URL: {}, 메소드: {} ===", 
                request.getRequestURI(), request.getMethod());
                
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = (UserDto) authentication.getPrincipal();
        
        Long companyId = userDto.getCompanyId();
        log.info("현재 사용자(회사 ID: {})의 GRI 데이터 조회 요청", companyId);
        
        // 요청 파라미터 및 헤더 로깅
        log.info("요청 헤더 - Content-Type: {}, Accept: {}, User-Agent: {}", 
                request.getHeader("Content-Type"), 
                request.getHeader("Accept"),
                request.getHeader("User-Agent"));
                
        Map<String, GriDataItemDto> griData = griDataItemService.getGriDataMapByCompanyId(companyId);
        log.info("GRI 데이터 조회 완료: {}개 항목 반환", griData.size());
        
        // 응답 캐시 방지 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.setPragma("no-cache");
        headers.setExpires(0L); // 만료 시간을 0으로 설정
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(griData);
    }
    
    /**
     * 현재 사용자 회사의 모든 GRI 데이터 일괄 업데이트
     *
     * @param griData GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     * @return 업데이트된 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 GRI 데이터 일괄 업데이트", description = "현재 로그인한 사용자 회사의 모든 GRI 데이터 항목을 일괄 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터를 업데이트하고 결과를 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @PutMapping("/company/gri")
    public ResponseEntity<Map<String, GriDataItemDto>> updateCompanyGriData(
            @Parameter(description = "업데이트할 GRI 데이터 맵", required = true) 
            @RequestBody Map<String, GriDataItemDto> griData,
            HttpServletRequest request) {
        
        log.info("=== 회사 GRI 데이터 업데이트 API 호출 - 요청 URL: {}, 메소드: {} ===", 
                request.getRequestURI(), request.getMethod());
        
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = (UserDto) authentication.getPrincipal();
        
        Long companyId = userDto.getCompanyId();
        log.info("현재 사용자(회사 ID: {})의 GRI 데이터 일괄 업데이트 요청. 항목 수: {}", companyId, griData.size());
        
        // 요청 데이터 샘플 로깅 (최대 2개 항목만)
        if (!griData.isEmpty()) {
            int count = 0;
            for (Map.Entry<String, GriDataItemDto> entry : griData.entrySet()) {
                if (count >= 2) break;
                log.info("업데이트 요청 데이터 샘플 - 키: {}, 값: {}", entry.getKey(), entry.getValue());
                count++;
            }
        }
        
        Map<String, GriDataItemDto> updatedData = griDataItemService.updateGriDataForCompany(companyId, griData);
        log.info("GRI 데이터 업데이트 완료: {}개 항목 처리됨", updatedData.size());
        
        // 응답 캐시 방지 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.setPragma("no-cache");
        headers.setExpires(0L);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(updatedData);
    }
    
    /**
     * 현재 사용자 회사의 특정 카테고리 GRI 데이터 업데이트
     *
     * @param category 카테고리 (E, S, G)
     * @param griData GRI 공시 코드를 키로 하는 GRI 데이터 항목 맵
     * @return 업데이트된 GRI 데이터 항목 맵
     */
    @Operation(summary = "회사별 카테고리 GRI 데이터 업데이트", description = "현재 로그인한 사용자 회사의 특정 카테고리(E, S, G) GRI 데이터 항목을 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 GRI 데이터를 업데이트하고 결과를 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회사를 찾을 수 없습니다.", content = @Content)
    })
    @PutMapping("/company/gri/{category}")
    public ResponseEntity<Map<String, GriDataItemDto>> updateCompanyGriDataByCategory(
            @Parameter(description = "카테고리 (E, S, G)", required = true) 
            @PathVariable String category,
            @Parameter(description = "업데이트할 GRI 데이터 맵", required = true) 
            @RequestBody Map<String, GriDataItemDto> griData) {
        
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = (UserDto) authentication.getPrincipal();
        
        Long companyId = userDto.getCompanyId();
        log.debug("현재 사용자(회사 ID: {})의 {} 카테고리 GRI 데이터 업데이트 요청. 항목 수: {}", companyId, category, griData.size());
        
        // 카테고리별 필터링
        Map<String, GriDataItemDto> filteredData = griData.entrySet().stream()
                .filter(entry -> {
                    GriDataItemDto dto = entry.getValue();
                    // 카테고리 일치 여부 확인 (E, S, G 중 하나)
                    return matchesCategory(dto.getCategory(), category);
                })
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        Map<String, GriDataItemDto> updatedData = griDataItemService.updateGriDataForCompany(companyId, filteredData);
        
        // 응답 캐시 방지 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.setPragma("no-cache");
        headers.setExpires(0L); // 만료 시간을 0으로 설정
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(updatedData);
    }
    
    /**
     * 카테고리 문자와 실제 카테고리의 일치 여부 확인
     */
    private boolean matchesCategory(String fullCategory, String categoryChar) {
        if (fullCategory == null || categoryChar == null) {
            return false;
        }
        
        switch (categoryChar.toUpperCase()) {
            case "E":
                return fullCategory.startsWith("Environment");
            case "S":
                return fullCategory.startsWith("Social");
            case "G":
                return fullCategory.startsWith("Governance");
            default:
                return false;
        }
    }
    
    /**
     * 현재 사용자 회사별 GRI 데이터 페이지네이션 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 페이지네이션이 적용된 GRI 데이터 항목 목록
     */
    @Operation(summary = "회사별 페이지네이션 GRI 데이터 조회", 
              description = "현재 로그인한 사용자 회사의 GRI 데이터 항목을 페이지 단위로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "페이지네이션이 적용된 GRI 데이터 항목 목록을 반환합니다.")
    @GetMapping("/company/gri/paged")
    public ResponseEntity<PageResponse<GriDataItemDto>> getCompanyGriDataPaginated(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (형식: 속성,정렬방향) 예: disclosureCode,asc", example = "disclosureCode,asc") 
            @RequestParam(required = false) String sort) {
        
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = (UserDto) authentication.getPrincipal();
        
        Long companyId = userDto.getCompanyId();
        log.debug("현재 사용자(회사 ID: {})의 페이지네이션 GRI 데이터 조회 요청: 페이지={}, 크기={}, 정렬={}", companyId, page, size, sort);
        
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
        
        // 회사별 GRI 데이터 페이지네이션 조회
        GriDataSearchCriteria criteria = GriDataSearchCriteria.builder()
                .companyId(companyId)
                .build();
        
        PageResponse<GriDataItemDto> result = griDataItemService.findByCriteria(criteria, pageable);
        
        // 응답 캐시 방지 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.setPragma("no-cache");
        headers.setExpires(0L); // 만료 시간을 0으로 설정
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(result);
    }
} 