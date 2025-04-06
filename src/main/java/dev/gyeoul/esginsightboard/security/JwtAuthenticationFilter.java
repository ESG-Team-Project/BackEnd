package dev.gyeoul.esginsightboard.security;

import dev.gyeoul.esginsightboard.config.JwtConfig;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

/**
 * JWT 인증 필터
 * <p>
 * 요청의 Authorization 헤더에서 JWT 토큰을 추출하고 검증하여 인증 처리를 수행합니다.
 * </p>
 */
@Component
@Order(1)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    @Lazy
    private UserService userService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private JwtConfig jwtConfig;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico",
            "/static",
            "/h2-console"
    );

    /**
     * 필터 처리를 수행합니다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException      IO 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 인증이 필요없는 경로는 처리하지 않음
        log.debug("JwtAuthenticationFilter.doFilterInternal - request: {}", request.getRequestURI());
        final String requestTokenHeader = request.getHeader(jwtConfig.getHeader());

        String email = null;
        String jwtToken = null;

        // JWT 토큰은 "Bearer token" 형태로 전달됨. Bearer 접두사 제거 필요
        if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtConfig.getPrefix() + " ")) {
            jwtToken = requestTokenHeader.substring(jwtConfig.getPrefix().length() + 1);
            try {
                email = jwtTokenUtil.getEmailFromToken(jwtToken);
            } catch (Exception e) {
                // JwtExceptionFilter에서 처리하므로 여기서는 로그만 남김
                log.trace("JWT Token 에러: {}", e.getMessage());
            }
        } else {
            log.trace("JWT 토큰이 Bearer로 시작하지 않거나 존재하지 않습니다.");
        }

        // 토큰 검증 및 인증 처리
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<UserDto> userOptional = userService.getUserByEmail(email);
            
            if (userOptional.isPresent() && jwtTokenUtil.validateToken(jwtToken)) {
                UserDto userDto = userOptional.get();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDto, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("인증 성공: {}", email);
            }
        }

        // 테스트 사용자 특별 처리
        if (email != null && email.equals("test@example.com") && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateTestUser(request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 테스트 사용자 인증 처리
     */
    private void authenticateTestUser(HttpServletRequest request) {
        // 테스트용 권한 설정
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "test@example.com", null, Collections.singletonList(authority));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("테스트 사용자 인증 성공");
    }
} 