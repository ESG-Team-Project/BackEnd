package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그를 위한 데이터 액세스 레이어
 * <p>
 * 감사 로그 엔티티에 대한 CRUD 작업과 사용자 정의 조회 기능을 제공합니다.
 * </p>
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 특정 엔티티 유형에 대한 감사 로그를 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * 특정 엔티티 유형과 ID에 대한 감사 로그를 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param entityId 엔티티 ID
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    /**
     * 특정 사용자에 의한 감사 로그를 조회합니다.
     *
     * @param username 사용자명
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    /**
     * 특정 액션에 대한 감사 로그를 조회합니다.
     *
     * @param action 액션
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * 특정 기간 내의 감사 로그를 조회합니다.
     *
     * @param startDateTime 시작 일시
     * @param endDateTime 종료 일시
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    /**
     * 특정 IP 주소에서 발생한 감사 로그를 조회합니다.
     *
     * @param ipAddress IP 주소
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * 특정 엔티티 유형과 액션에 대한 감사 로그를 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param action 액션
     * @param pageable 페이지 정보
     * @return 페이지네이션이 적용된 감사 로그 목록
     */
    Page<AuditLog> findByEntityTypeAndAction(String entityType, String action, Pageable pageable);

    /**
     * 특정 엔티티의 최근 로그를 조회합니다.
     *
     * @param entityType 엔티티 유형
     * @param entityId 엔티티 ID
     * @param limit 조회할 로그 개수
     * @return 최근 감사 로그 목록
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentLogsByEntity(@Param("entityType") String entityType, @Param("entityId") String entityId, Pageable pageable);

    /**
     * 특정 사용자의 최근 활동을 조회합니다.
     *
     * @param username 사용자명
     * @param limit 조회할 로그 개수
     * @return 최근 감사 로그 목록
     */
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentLogsByUsername(@Param("username") String username, Pageable pageable);

    /**
     * 엔티티 타입 및 ID 포함 문자열로 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId LIKE %:entityIdContaining%")
    Page<AuditLog> findByEntityTypeAndEntityIdContaining(
            @Param("entityType") String entityType, 
            @Param("entityIdContaining") String entityIdContaining, 
            Pageable pageable);
} 