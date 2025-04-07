package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 검증 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 검증 응답")
public class TokenVerificationResponse {
    @Schema(description = "토큰 유효 여부", example = "true")
    private boolean valid;
    
    @Schema(description = "사용자 이름 (이메일)", example = "user@example.com", nullable = true)
    private String username;
    
    @Schema(description = "응답 메시지", example = "토큰이 유효합니다", nullable = true)
    private String message;
} 