package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.security.JwtAuthenticationFilter;
import dev.gyeoul.esginsightboard.security.JwtExceptionFilter;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 보안 설정 구성 클래스
 * <p>
 * 인증, 인가, CORS, JWT 필터 등 보안 관련 설정을 정의합니다.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    
    @Lazy
    private final UserService userService;
    
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    /**
     * 보안 필터 체인 설정
     *
     * @param http HTTP 보안 설정
     * @return 보안 필터 체인
     * @throws Exception 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("보안 필터 체인 구성 중...");
        
        // JWT 필터 생성
        JwtExceptionFilter jwtExceptionFilter = jwtExceptionFilter();
        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilter();
        
        http
            .csrf(AbstractHttpConfigurer::disable)  // JWT 기반 인증이므로 CSRF 비활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))  // H2 콘솔 사용을 위한 설정
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // JWT 사용으로 세션을 STATELESS로 설정
            .requestCache(RequestCacheConfigurer::disable)  // 요청 캐시 비활성화
            .authorizeHttpRequests(auth -> auth
                // 인증 불필요 API
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico", "/h2-console/**").permitAll()  // API 문서 관련 경로
                .requestMatchers("/api/auth/**").permitAll()  // 인증 관련 API
                .requestMatchers("/error").permitAll()  // 오류 페이지
                .requestMatchers("/static/**").permitAll()  // 정적 리소스
                
                // 인증 필요 API
                .requestMatchers("/api/documents/**").authenticated()  // 문서 관련 API
                .requestMatchers("/api/companies/**").authenticated()  // 회사 관련 API
                .requestMatchers("/api/users/**").authenticated()  // 사용자 관련 API
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );
            
        // JWT 관련 필터 추가
        http.addFilterBefore(jwtExceptionFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * JWT 인증 필터 생성
     * 
     * @return JWT 인증 필터
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        log.info("JWT 인증 필터 생성");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        return filter;
    }
    
    /**
     * JWT 예외 처리 필터 생성
     * 
     * @return JWT 예외 처리 필터
     */
    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        log.info("JWT 예외 처리 필터 생성");
        return new JwtExceptionFilter();
    }

    /**
     * CORS 설정
     *
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));  // 모든 오리진 허용
        configuration.setAllowedMethods(Arrays.asList(  // 허용할 HTTP 메서드
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(Arrays.asList(  // 허용할 헤더
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        configuration.setMaxAge(3600L);  // pre-flight 요청 캐시 시간(초)
        configuration.setExposedHeaders(Arrays.asList(  // 응답에서 노출할 헤더
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                "X-Custom-Header"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 설정 적용
        return source;
    }

    /**
     * 인증 제공자 설정
     *
     * @return 인증 제공자
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * 인증 관리자 설정
     *
     * @return 인증 관리자
     * @throws Exception 예외
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
} 
