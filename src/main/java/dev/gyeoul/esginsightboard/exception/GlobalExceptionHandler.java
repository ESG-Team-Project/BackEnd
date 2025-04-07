package dev.gyeoul.esginsightboard.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 컨트롤러 어드바이스
 * <p>
 * 애플리케이션 전체에서 발생하는 예외를 일관된 형식으로 처리합니다.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * API 오류 응답 객체
     */
    @Schema(description = "API 오류 응답")
    public static class ApiError {
        @Schema(description = "오류 발생 시간", example = "2025-04-07T10:15:30.123456789")
        public final LocalDateTime timestamp = LocalDateTime.now();
        
        @Schema(description = "HTTP 상태 코드", example = "400")
        public final int status;
        
        @Schema(description = "HTTP 상태 메시지", example = "Bad Request")
        public final String error;
        
        @Schema(description = "오류 메시지", example = "입력값 검증에 실패했습니다")
        public final String message;
        
        @Schema(description = "상세 오류 정보", nullable = true)
        public Object details;

        public ApiError(HttpStatus status, String message) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
        }

        public ApiError(HttpStatus status, String message, Object details) {
            this(status, message);
            this.details = details;
        }
    }

    /**
     * 문서 처리 관련 예외 처리
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiError> handleIOException(IOException ex) {
        log.error("파일 처리 중 오류 발생: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 요청 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("파라미터 타입 불일치: {}", ex.getMessage());
        
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("파라미터 '%s'의 값이 '%s' 타입으로 변환될 수 없습니다", 
                paramName, requiredType);
        
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, message, ex.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 유효성 검사 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("유효성 검증 실패: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 필수 요청 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("필수 파라미터 누락: {}", ex.getMessage());
        
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, 
                String.format("필수 파라미터 '%s'이(가) 누락되었습니다", ex.getParameterName()));
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 파일 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("파일 크기 초과: {}", ex.getMessage());
        
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, "업로드된 파일이 최대 허용 크기를 초과했습니다");
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * JWT 관련 예외 처리
     */
    @ExceptionHandler({
        ExpiredJwtException.class,
        UnsupportedJwtException.class,
        MalformedJwtException.class,
        SignatureException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleJwtException(Exception ex) {
        log.warn("JWT 처리 오류: {}", ex.getMessage());
        
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String message = "인증 토큰 오류";
        
        if (ex instanceof ExpiredJwtException) {
            message = "인증 토큰이 만료되었습니다";
        } else if (ex instanceof UnsupportedJwtException) {
            message = "지원하지 않는 인증 토큰입니다";
        } else if (ex instanceof MalformedJwtException || ex instanceof SignatureException) {
            message = "잘못된 형식의 인증 토큰입니다";
        }
        
        ApiError error = new ApiError(status, message);
        
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 인증 관련 예외 처리
     */
    @ExceptionHandler({
        InsufficientAuthenticationException.class,
        BadCredentialsException.class,
        AccessDeniedException.class
    })
    public ResponseEntity<ApiError> handleAuthenticationException(Exception ex) {
        log.warn("인증/인가 오류: {}", ex.getMessage());
        
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String message = "인증에 실패했습니다";
        
        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = "해당 리소스에 접근할 권한이 없습니다";
        } else if (ex instanceof BadCredentialsException) {
            message = "아이디 또는 비밀번호가 일치하지 않습니다";
        }
        
        ApiError error = new ApiError(status, message);
        
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        // 문서 생성 예외인 경우 특별히 처리
        String exceptionClassName = ex.getClass().getName();
        if ("com.itextpdf.text.DocumentException".equals(exceptionClassName)) {
            log.error("문서 생성 중 오류 발생: {}", ex.getMessage(), ex);
            ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "문서 생성 중 오류가 발생했습니다", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
        
        // 그 외 일반 예외
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
} 