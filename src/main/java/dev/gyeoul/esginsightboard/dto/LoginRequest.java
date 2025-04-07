package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 정보를 담는 DTO 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "사용자 비밀번호", example = "password123", required = true)
    private String password;
} 