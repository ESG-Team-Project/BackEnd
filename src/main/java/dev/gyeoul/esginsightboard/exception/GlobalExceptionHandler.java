package dev.gyeoul.esginsightboard.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 전체에서 발생하는 예외를 처리하는 전역 예외 처리기
 * 
 * <p>
 * 다음과 같은 오류 응답 형식을 제공합니다:
 * <pre>
 * {
 *   "success": false,
 *   "message": "오류 메시지",
 *   "error": "오류 코드",
 *   "errors": {
 *     "필드명": "유효성 검증 실패 메시지"
 *   }
 * }
 * </pre>
 * </p>
 */
@Slf4j
@RestControllerAdvice
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "400", 
        description = "유효성 검증 실패",
        content = @Content(mediaType = "application/json", 
            schema = @Schema(example = """
                {
                  "success": false,
                  "message": "입력값 유효성 검증에 실패했습니다",
                  "errors": {
                    "file": "CSV 파일은 필수입니다",
                    "email": "유효한 이메일 주소를 입력해주세요",
                    "password": "비밀번호는 8자 이상 30자 이하여야 합니다",
                    "name": "이름은 필수입니다",
                    "companyName": "회사명은 필수입니다"
                  },
                  "error": "VALIDATION_FAILED"
                }
                """))
    ),
    @ApiResponse(
        responseCode = "404", 
        description = "리소스를 찾을 수 없음",
        content = @Content(mediaType = "application/json", 
            schema = @Schema(example = """
                {
                  "success": false,
                  "message": "ID가 1인 회사를 찾을 수 없습니다.",
                  "error": "RESOURCE_NOT_FOUND"
                }
                """))
    ),
    @ApiResponse(
        responseCode = "500", 
        description = "서버 내부 오류",
        content = @Content(mediaType = "application/json", 
            schema = @Schema(example = """
                {
                  "success": false,
                  "message": "서버 내부 오류가 발생했습니다",
                  "error": "INTERNAL_SERVER_ERROR"
                }
                """))
    )
})
public class GlobalExceptionHandler {

    /**
     * 입력값 유효성 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("유효성 검증 실패: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        errorResponse.put("success", false);
        errorResponse.put("message", "입력값 유효성 검증에 실패했습니다");
        errorResponse.put("errors", errors);
        errorResponse.put("error", "VALIDATION_FAILED");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 사용자가 이미 존재할 때 발생하는 예외 처리
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        log.error("이미 존재하는 사용자: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("error", "USER_ALREADY_EXISTS");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * 사용자를 찾을 수 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        log.error("사용자를 찾을 수 없음: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("error", "USER_NOT_FOUND");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * 인증 실패(비밀번호 불일치 등) 예외 처리
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("인증 실패: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "이메일 또는 비밀번호가 일치하지 않습니다");
        errorResponse.put("error", "INVALID_CREDENTIALS");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * 서비스 레벨에서 발생하는 IllegalArgumentException 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("잘못된 입력값: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("error", "INVALID_ARGUMENT");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("서버 오류 발생: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "서버 내부 오류가 발생했습니다");
        errorResponse.put("error", "INTERNAL_SERVER_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 