package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.security.JwtAuthenticationFilter;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JwtFilterConfig {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtConfig jwtConfig;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userService, jwtTokenUtil, jwtConfig);
    }
} 