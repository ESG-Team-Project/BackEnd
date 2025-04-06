package dev.gyeoul.esginsightboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더 설정 클래스
 * <p>
 * 비밀번호 암호화를 위한 인코더 빈을 정의합니다.
 * 순환 참조 문제를 해결하기 위해 SecurityConfig에서 분리했습니다.
 * </p>
 */
@Configuration
public class PasswordConfig {

    /**
     * 비밀번호 인코더 빈 정의
     *
     * @return 비밀번호 인코더 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 