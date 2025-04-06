package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.AuditLogDto;
import dev.gyeoul.esginsightboard.dto.PageResponse;
import dev.gyeoul.esginsightboard.entity.AuditLog;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.repository.AuditLogRepository;
import dev.gyeoul.esginsightboard.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 감사 로그 서비스
 * <p>
 * 이 서비스는 시스템 내의 다양한 액션을 기록하기 위한 감사 로깅 기능을 제공합니다.
 * 사용자 활동, 데이터 변경 등을 추적하여 보안 감사 및 데이터 변경 이력 관리에 활용됩니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    /**
     * 감사 로그를 기록합니다.
     *
     * @param entityType 엔티티 유형 (예: "GriDataItem", "User")
     * @param entityId 엔티티 ID
     * @param action 수행된 액션 (예: "생성", "수정", "삭제")
     * @param details 상세 정보 (선택적)
     */
    @Transactional
    public void logActivity(String entityType, Long entityId, AuditLog.AuditAction action, String details) {
        log.debug("감사 로그 저장: {}, {}, {}", entityType, entityId, action);
        
        try {
            String username = getCurrentUsername();
            String ipAddress = getClientIpAddress();
            
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId.toString())
                    .action(action.name())
                    .details(details)
                    .username(username)
                    .ipAddress(ipAddress)
                    .user(getCurrentUser().orElse(null))
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("감사 로그 저장 완료: {}, {}, {}", entityType, entityId, action);
        } catch (Exception e) {
            // 로깅 실패가 주 기능을 방해하지 않도록 예외를 잡아서 처리
            log.error("감사 로그 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 페이지네이션이 적용된 감사 로그 목록을 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogs(Pageable pageable) {
        log.debug("페이지네이션 감사 로그 조회: 페이지={}, 크기={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AuditLogDto> result = auditLogRepository.findAll(pageable)
                .map(this::mapToDto);
        return PageResponse.from(result);
    }

    /**
     * 특정 엔티티 유형의 감사 로그 목록을 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 특정 엔티티 유형의 감사 로그 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        log.debug("엔티티 유형별 감사 로그 조회: 유형={}, 페이지={}", entityType, pageable.getPageNumber());
        Page<AuditLogDto> result = auditLogRepository.findByEntityType(entityType, pageable)
                .map(this::mapToDto);
        return PageResponse.from(result);
    }

    /**
     * 특정 엔티티의 감사 로그 목록을 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param entityId 엔티티 ID
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 특정 엔티티의 감사 로그 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        log.debug("엔티티별 감사 로그 조회: 유형={}, ID={}, 페이지={}", entityType, entityId, pageable.getPageNumber());
        Page<AuditLogDto> result = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId.toString(), pageable)
                .map(this::mapToDto);
        return PageResponse.from(result);
    }

    /**
     * 특정 사용자의 감사 로그 목록을 조회합니다.
     *
     * @param username 사용자명
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 특정 사용자의 감사 로그 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogsByUsername(String username, Pageable pageable) {
        log.debug("사용자별 감사 로그 조회: 사용자={}, 페이지={}", username, pageable.getPageNumber());
        Page<AuditLogDto> result = auditLogRepository.findByUsername(username, pageable)
                .map(this::mapToDto);
        return PageResponse.from(result);
    }

    /**
     * 특정 기간 내의 감사 로그 목록을 조회합니다.
     *
     * @param startDateTime 시작 일시
     * @param endDateTime 종료 일시
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 특정 기간 내의 감사 로그 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogsByDateRange(
            LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        log.debug("기간별 감사 로그 조회: 시작={}, 종료={}, 페이지={}", 
                  startDateTime, endDateTime, pageable.getPageNumber());
        Page<AuditLogDto> result = auditLogRepository.findByCreatedAtBetween(startDateTime, endDateTime, pageable)
                .map(this::mapToDto);
        return PageResponse.from(result);
    }

    /**
     * 현재 로그인한 사용자의 ID를 가져옵니다.
     *
     * @return 현재 로그인한 사용자 (Optional)
     */
    private Optional<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    return userRepository.findByEmail(username);
                }
            }
        } catch (Exception e) {
            log.warn("현재 사용자 정보 가져오기 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 현재 로그인한 사용자의 이름을 가져옵니다.
     *
     * @return 현재 로그인한 사용자명 또는 "SYSTEM"
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("현재 사용자명 가져오기 실패: {}", e.getMessage());
        }
        return "SYSTEM";
    }

    /**
     * 클라이언트의 IP 주소를 가져옵니다.
     *
     * @return 클라이언트 IP 주소
     */
    private String getClientIpAddress() {
        // X-Forwarded-For 헤더 확인 (프록시/로드밸런서 환경)
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        // 다른 헤더들 확인
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        // 직접 IP 주소 가져오기
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 여러 IP가 있는 경우 첫 번째 것만 사용
        if (StringUtils.hasText(ipAddress) && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }

    /**
     * AuditLog 엔티티를 DTO로 변환합니다.
     *
     * @param auditLog 변환할 감사 로그 엔티티
     * @return 변환된 감사 로그 DTO
     */
    private AuditLogDto mapToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .details(auditLog.getDetails())
                .username(auditLog.getUsername())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    /**
     * 감사 로그 저장
     *
     * @param auditLogDto 저장할 감사 로그 DTO
     * @return 저장된 감사 로그 DTO
     */
    public AuditLogDto saveAuditLog(AuditLogDto auditLogDto) {
        log.debug("감사 로그 저장: {}", auditLogDto);
        
        AuditLog auditLog = AuditLog.builder()
                .entityType(auditLogDto.getEntityType())
                .entityId(auditLogDto.getEntityId())
                .action(auditLogDto.getAction())
                .details(auditLogDto.getDetails())
                .username(auditLogDto.getUsername())
                .ipAddress(auditLogDto.getIpAddress())
                .build();
        
        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        
        return AuditLogDto.builder()
                .id(savedAuditLog.getId())
                .entityType(savedAuditLog.getEntityType())
                .entityId(savedAuditLog.getEntityId())
                .action(savedAuditLog.getAction())
                .details(savedAuditLog.getDetails())
                .username(savedAuditLog.getUsername())
                .ipAddress(savedAuditLog.getIpAddress())
                .createdAt(savedAuditLog.getCreatedAt())
                .build();
    }

    /**
     * 엔티티 타입 및 ID로 감사 로그 조회
     *
     * @param entityType 엔티티 타입
     * @param entityId 엔티티 ID
     * @param pageable 페이징 정보
     * @return 감사 로그 페이지
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable) {
        log.debug("엔티티 타입 {} 및 ID {}로 감사 로그 조회", entityType, entityId);
        
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(auditLog -> AuditLogDto.builder()
                        .id(auditLog.getId())
                        .entityType(auditLog.getEntityType())
                        .entityId(auditLog.getEntityId())
                        .action(auditLog.getAction())
                        .details(auditLog.getDetails())
                        .username(auditLog.getUsername())
                        .ipAddress(auditLog.getIpAddress())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
    }

    /**
     * 엔티티 타입 및 ID 포함 여부로 감사 로그 조회
     *
     * @param entityType 엔티티 타입
     * @param entityIdContaining 엔티티 ID 포함 문자열
     * @param pageable 페이징 정보
     * @return 감사 로그 페이지 응답
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> findByEntityTypeAndEntityIdContaining(
            String entityType, String entityIdContaining, Pageable pageable) {
        log.debug("엔티티 타입 {} 및 ID 포함 문자열 {}로 감사 로그 조회", entityType, entityIdContaining);
        
        Page<AuditLogDto> result = auditLogRepository.findByEntityTypeAndEntityIdContaining(
                entityType, entityIdContaining, pageable)
                .map(auditLog -> AuditLogDto.builder()
                        .id(auditLog.getId())
                        .entityType(auditLog.getEntityType())
                        .entityId(auditLog.getEntityId())
                        .action(auditLog.getAction())
                        .details(auditLog.getDetails())
                        .username(auditLog.getUsername())
                        .ipAddress(auditLog.getIpAddress())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
        
        return PageResponse.from(result);
    }

    /**
     * 전체 감사 로그 조회 (페이지네이션)
     *
     * @param pageable 페이징 정보
     * @return 감사 로그 페이지 응답
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> findAllAuditLogs(Pageable pageable) {
        log.debug("전체 감사 로그를 페이지네이션으로 조회합니다. 페이지: {}, 크기: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        Page<AuditLogDto> result = auditLogRepository.findAll(pageable)
                .map(auditLog -> AuditLogDto.builder()
                        .id(auditLog.getId())
                        .entityType(auditLog.getEntityType())
                        .entityId(auditLog.getEntityId())
                        .action(auditLog.getAction())
                        .details(auditLog.getDetails())
                        .username(auditLog.getUsername())
                        .ipAddress(auditLog.getIpAddress())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
        
        return PageResponse.from(result);
    }
    
    /**
     * 엔티티 타입으로 감사 로그 조회
     *
     * @param entityType 엔티티 타입
     * @param pageable 페이징 정보
     * @return 감사 로그 페이지 응답
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> findByEntityType(String entityType, Pageable pageable) {
        log.debug("엔티티 타입 {}로 감사 로그를 조회합니다.", entityType);
        
        Page<AuditLogDto> result = auditLogRepository.findByEntityType(entityType, pageable)
                .map(auditLog -> AuditLogDto.builder()
                        .id(auditLog.getId())
                        .entityType(auditLog.getEntityType())
                        .entityId(auditLog.getEntityId())
                        .action(auditLog.getAction())
                        .details(auditLog.getDetails())
                        .username(auditLog.getUsername())
                        .ipAddress(auditLog.getIpAddress())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
        
        return PageResponse.from(result);
    }
    
    /**
     * 사용자명으로 감사 로그 조회
     *
     * @param username 사용자명
     * @param pageable 페이징 정보
     * @return 감사 로그 페이지 응답
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> findByUsername(String username, Pageable pageable) {
        log.debug("사용자 {}의 감사 로그를 조회합니다.", username);
        
        Page<AuditLogDto> result = auditLogRepository.findByUsername(username, pageable)
                .map(auditLog -> AuditLogDto.builder()
                        .id(auditLog.getId())
                        .entityType(auditLog.getEntityType())
                        .entityId(auditLog.getEntityId())
                        .action(auditLog.getAction())
                        .details(auditLog.getDetails())
                        .username(auditLog.getUsername())
                        .ipAddress(auditLog.getIpAddress())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
        
        return PageResponse.from(result);
    }
} 