package dev.gyeoul.esginsightboard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 예외 처리 필터
 * <p>
 * JWT 토큰 검증 과정에서 발생하는 예외를 처리하는 필터입니다.
 * </p>
 */
@Component
@Order(0)
@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

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
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 다시 로그인해주세요.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다.");
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "잘못된 토큰 형식입니다.");
        } catch (SignatureException e) {
            log.error("JWT 서명 검증 실패: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 클레임 문자열이 비어있음: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다.");
        } catch (Exception e) {
            log.error("JWT 처리 중 예상치 못한 오류: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 에러 응답을 설정합니다.
     *
     * @param response HTTP 응답
     * @param status   HTTP 상태
     * @param message  에러 메시지
     * @throws IOException IO 예외
     */
    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);

        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
} 