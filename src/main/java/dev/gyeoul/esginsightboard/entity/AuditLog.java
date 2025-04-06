package dev.gyeoul.esginsightboard.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 시스템 감사 로그를 저장하는 엔티티 클래스
 * <p>
 * 이 클래스는 시스템에서 발생하는 중요 이벤트 및 사용자 활동을 추적하기 위한 감사 로그를 저장합니다.
 * 감사 로깅을 통해 데이터 변경 이력, 사용자 행동, 정보 접근 등을 추적할 수 있습니다.
 * </p>
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "audit_logs", 
       indexes = {
           @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
           @Index(name = "idx_audit_entity_id", columnList = "entity_id"),
           @Index(name = "idx_audit_action", columnList = "action"),
           @Index(name = "idx_audit_created_at", columnList = "created_at"),
           @Index(name = "idx_audit_username", columnList = "username")
       })
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 감사 로그 이벤트 유형 열거형
     */
    public enum AuditAction {
        CREATE("생성"),
        READ("조회"),
        UPDATE("수정"),
        DELETE("삭제"),
        LOGIN("로그인"),
        LOGOUT("로그아웃"),
        EXPORT("내보내기"),
        IMPORT("가져오기"),
        VERIFY("검증"),
        APPROVE("승인"),
        REJECT("거부");

        private final String displayName;

        AuditAction(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 