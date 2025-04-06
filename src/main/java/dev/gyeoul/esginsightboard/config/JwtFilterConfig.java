package dev.gyeoul.esginsightboard.config;

import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

/**
 * JWT 필터 설정 클래스
 * <p>
 * 순환 참조 문제로 인해 직접 필터 빈 생성하는 대신, SecurityConfig에서 필터를 추가합니다.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class JwtFilterConfig {
    // 빈 클래스 - 순환 참조 문제를 해결하기 위해 직접 필터 빈 생성 대신 SecurityConfig에서 처리
} 