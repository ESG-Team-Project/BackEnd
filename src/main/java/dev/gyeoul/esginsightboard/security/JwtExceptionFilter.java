package dev.gyeoul.esginsightboard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인증 중 발생하는 예외를 처리하는 필터
 * <p>
 * JWT 토큰 검증 중 발생하는 다양한 예외를 캐치하여 클라이언트에게
 * 적절한 오류 응답을 반환합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "지원하지 않는 인증 토큰입니다");
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "잘못된 형식의 인증 토큰입니다");
        } catch (SignatureException e) {
            log.warn("JWT 서명 검증 실패: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증 토큰 서명이 유효하지 않습니다");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 처리 오류: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증 토큰 처리 중 오류가 발생했습니다");
        } catch (Exception e) {
            log.error("인증 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
        }
    }

    /**
     * 클라이언트에게 오류 응답을 반환합니다.
     * 
     * @param response HTTP 응답
     * @param status HTTP 상태 코드
     * @param message 오류 메시지
     */
    private void setErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message) throws IOException {
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", "JWT 인증");
        
        String errorJson = objectMapper.writeValueAsString(errorDetails);
        response.getWriter().write(errorJson);
    }
} 