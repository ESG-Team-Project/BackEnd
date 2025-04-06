package dev.gyeoul.esginsightboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 감사 로그 정보를 전달하기 위한 DTO 클래스
 * <p>
 * 이 클래스는 시스템 감사 로그를 API 응답으로 전달하기 위해 사용됩니다.
 * 엔티티 타입, 수행된 작업, 수행 시간 등의 정보를 포함합니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "감사 로그 정보")
public class AuditLogDto {

    @Schema(description = "감사 로그 ID", example = "1")
    private Long id;

    @Schema(description = "엔티티 유형", example = "GriDataItem")
    private String entityType;

    @Schema(description = "엔티티 ID", example = "42")
    private String entityId;

    @Schema(description = "수행된 작업", example = "CREATE")
    private String action;

    @Schema(description = "상세 정보")
    private String details;

    @Schema(description = "사용자명", example = "john.doe@example.com")
    private String username;

    @Schema(description = "IP 주소", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "생성 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
} 