package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.AuditLogDto;
import dev.gyeoul.esginsightboard.dto.PageResponse;
import dev.gyeoul.esginsightboard.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 감사 로그 API 컨트롤러
 * <p>
 * 시스템 내 데이터 변경 내역에 대한 감사 로그를 조회하는 API를 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "감사 로그", description = "시스템 데이터 변경 내역 감사 로그 API")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 페이지네이션을 적용한 전체 감사 로그 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    @Operation(summary = "전체 감사 로그 조회", description = "시스템 전체의 감사 로그를 페이지네이션을 적용하여 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogDto>> getAllAuditLogs(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (형식: 속성,정렬방향)", example = "createdAt,desc") 
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        log.debug("전체 감사 로그 조회 요청: 페이지={}, 크기={}, 정렬={}", page, size, sort);
        
        // 정렬 설정 처리
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        // 감사 로그 조회 (auditLogService에서 PageResponse로 변환하는 메소드 필요)
        PageResponse<AuditLogDto> result = auditLogService.findAllAuditLogs(pageable);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 엔티티 타입의 감사 로그 조회
     *
     * @param entityType 엔티티 타입
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    @Operation(summary = "엔티티 타입별 감사 로그 조회", description = "특정 엔티티 타입(예: GriDataItem, User)의 감사 로그를 조회합니다.")
    @GetMapping("/type/{entityType}")
    public ResponseEntity<PageResponse<AuditLogDto>> getAuditLogsByEntityType(
            @Parameter(description = "엔티티 타입", example = "GriDataItem") 
            @PathVariable String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        log.debug("엔티티 타입 {}의 감사 로그 조회 요청: 페이지={}, 크기={}, 정렬={}", entityType, page, size, sort);
        
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        PageResponse<AuditLogDto> result = auditLogService.findByEntityType(entityType, pageable);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 엔티티의 감사 로그 조회
     *
     * @param entityType 엔티티 타입
     * @param entityId 엔티티 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    @Operation(summary = "특정 엔티티의 감사 로그 조회", description = "특정 엔티티(타입 및 ID)에 대한 감사 로그를 조회합니다.")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsByEntity(
            @Parameter(description = "엔티티 타입", example = "GriDataItem")
            @PathVariable String entityType,
            @Parameter(description = "엔티티 ID", example = "123")
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        log.debug("엔티티 타입 {} ID {}의 감사 로그 조회 요청: 페이지={}, 크기={}, 정렬={}", 
                 entityType, entityId, page, size, sort);
        
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        Page<AuditLogDto> result = auditLogService.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 사용자의 감사 로그 조회
     *
     * @param username 사용자명
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    @Operation(summary = "사용자별 감사 로그 조회", description = "특정 사용자가 수행한 작업의 감사 로그를 조회합니다.")
    @GetMapping("/user/{username}")
    public ResponseEntity<PageResponse<AuditLogDto>> getAuditLogsByUsername(
            @Parameter(description = "사용자명", example = "admin@example.com")
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        log.debug("사용자 {}의 감사 로그 조회 요청: 페이지={}, 크기={}, 정렬={}", username, page, size, sort);
        
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        PageResponse<AuditLogDto> result = auditLogService.findByUsername(username, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Sort 객체 생성 유틸리티 메소드
     */
    private Sort createSortObject(String sortStr) {
        if (sortStr == null || sortStr.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String[] parts = sortStr.split(",");
        String property = parts[0];
        
        if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) {
            return Sort.by(Sort.Direction.ASC, property);
        } else {
            return Sort.by(Sort.Direction.DESC, property);
        }
    }
} 