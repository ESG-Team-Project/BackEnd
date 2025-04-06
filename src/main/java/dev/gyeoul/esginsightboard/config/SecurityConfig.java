package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.security.JwtAuthenticationFilter;
import dev.gyeoul.esginsightboard.security.JwtExceptionFilter;
import dev.gyeoul.esginsightboard.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 보안 설정 클래스
 * <p>
 * 스프링 시큐리티 설정을 정의합니다. JWT 인증, CORS, CSRF, 세션 관리 등에 대한
 * 설정을 포함합니다.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/api/public/**", 
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/static/**",
            "/error",
            "/favicon.ico"
    };

    /**
     * 보안 필터 체인 설정
     * <p>
     * HTTP 보안 설정을 구성하고 필터를 등록합니다.
     * </p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 공개 경로 설정
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Swagger UI 및 API 문서 접근 허용
                        .requestMatchers("/swagger-ui.html").permitAll()
                        // 정적 리소스 접근 허용
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/documents/**").permitAll()
                        // API 접근 권한 설정
                        .requestMatchers("/api/document/**").authenticated()
                        .requestMatchers("/api/company/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정을 제공합니다.
     * <p>
     * 브라우저의 보안 정책에 따라 다른 출처에서의 요청을 허용하는 설정입니다.
     * </p>
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of(
                HttpHeaders.CONTENT_DISPOSITION,
                "X-Auth-Token",
                "X-Download-Progress"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 인증 제공자 설정
     * <p>
     * 사용자 인증을 위한 제공자를 설정합니다.
     * </p>
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 비밀번호 인코더
     * <p>
     * 비밀번호를 안전하게 저장하기 위한 인코더를 제공합니다.
     * </p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 관리자
     * <p>
     * 인증 프로세스를 관리하는 관리자를 제공합니다.
     * </p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
} 
