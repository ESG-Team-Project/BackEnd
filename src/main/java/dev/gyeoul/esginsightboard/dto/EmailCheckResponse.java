package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이메일 중복 체크 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 중복 체크 응답")
public class EmailCheckResponse {
    @Schema(description = "이메일 사용 가능 여부 (true: 사용 가능, false: 중복으로 사용 불가)", example = "true")
    private boolean available;
    
    @Schema(description = "응답 메시지", example = "사용 가능한 이메일입니다")
    private String message;
} 