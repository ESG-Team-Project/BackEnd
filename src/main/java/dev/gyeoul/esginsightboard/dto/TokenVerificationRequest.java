package dev.gyeoul.esginsightboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 검증 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationRequest {
    private String token;
} 