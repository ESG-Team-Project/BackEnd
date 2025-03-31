package dev.gyeoul.esginsightboard.security;

import dev.gyeoul.esginsightboard.config.JwtConfig;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtConfig jwtConfig;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/swagger-ui", 
        "/api-docs", 
        "/v3/api-docs",
        "/api/users/test-token",
        "/api/users/login",
        "/api/users/signup"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 화이트리스트 경로 확인
        String requestPath = request.getRequestURI();
        if (EXCLUDED_PATHS.stream().anyMatch(requestPath::contains)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더에서 JWT 토큰 추출
        final String authHeader = request.getHeader(jwtConfig.getHeader());
        log.debug("요청 헤더: {}", authHeader);

        String email = null;
        String jwtToken = null;

        // JWT 토큰 파싱 (Bearer 접두사 제거)
        if (authHeader != null && authHeader.startsWith(jwtConfig.getPrefix())) {
            jwtToken = authHeader.substring(jwtConfig.getPrefix().length());
            log.debug("파싱된 토큰: {}", jwtToken);

            try {
                email = jwtTokenUtil.getEmailFromToken(jwtToken);
                log.debug("토큰에서 추출한 이메일: {}", email);
            } catch (Exception e) {
                log.warn("토큰을 파싱할 수 없습니다: {}", e.getMessage());
            }
        } else {
            log.debug("JWT 토큰이 없거나 Bearer로 시작하지 않습니다: {}", authHeader);
        }

        // 이메일을 얻었고, 현재 인증된 사용자가 없는 경우에만 처리
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(request, email, jwtToken);
        }

        // 테스트 사용자 특별 처리
        if (email != null && email.equals("test@example.com") && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateTestUser(request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 사용자 인증 처리
     */
    private void authenticateUser(HttpServletRequest request, String email, String jwtToken) {
        Optional<UserDto> userOptional = userService.getUserByEmail(email);

        if (userOptional.isPresent() && jwtTokenUtil.validateToken(jwtToken)) {
            UserDto userDto = userOptional.get();
            UsernamePasswordAuthenticationToken authentication = createAuthToken(userDto);
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 이전 버전과의 호환성을 위해 request에도 사용자 정보 설정
            request.setAttribute("user", userDto);
            log.info("사용자 '{}' 인증 성공", email);
        } else {
            log.warn("유효하지 않은 토큰이거나 사용자를 찾을 수 없습니다: {}", email);
        }
    }

    /**
     * 테스트 사용자 인증 처리
     */
    private void authenticateTestUser(HttpServletRequest request) {
        log.info("테스트 사용자 인증 적용 중: test@example.com");
        UserDto testUser = new UserDto();
        testUser.setId(999L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");
        testUser.setCompanyName("테스트 회사");
        
        UsernamePasswordAuthenticationToken authentication = createAuthToken(testUser);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        request.setAttribute("user", testUser);
    }

    /**
     * 인증 토큰 생성
     */
    private UsernamePasswordAuthenticationToken createAuthToken(UserDto userDto) {
        return new UsernamePasswordAuthenticationToken(
            userDto, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
} 