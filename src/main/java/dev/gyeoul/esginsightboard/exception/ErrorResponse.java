package dev.gyeoul.esginsightboard.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API 오류 응답을 표준화하기 위한 DTO 클래스
 * <p>
 * 이 클래스는 API에서 발생하는 다양한 오류 정보를 클라이언트에 전달하는 용도로 사용됩니다.
 * 오류 코드, 메시지, 발생 시간 등의 정보를 포함합니다.
 * </p>
 */
@Getter
@Builder
@Schema(description = "오류 응답")
public class ErrorResponse {

    @Schema(description = "오류 상태 코드", example = "400")
    private final int status;

    @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
    private final String code;

    @Schema(description = "오류 메시지", example = "입력값이 유효하지 않습니다.")
    private final String message;

    @Schema(description = "오류 발생 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(description = "오류 발생 경로", example = "/api/gri/1")
    private final String path;

    @Schema(description = "상세 오류 정보")
    @Builder.Default
    private final List<ValidationError> errors = new ArrayList<>();

    /**
     * 유효성 검증 오류 정보를 위한 내부 클래스
     */
    @Getter
    @Builder
    @Schema(description = "유효성 검증 오류 정보")
    public static class ValidationError {

        @Schema(description = "오류가 발생한 필드", example = "email")
        private final String field;

        @Schema(description = "거부된 값", example = "invalid-email")
        private final String rejectedValue;

        @Schema(description = "오류 메시지", example = "올바른 이메일 형식이 아닙니다.")
        private final String message;
    }
} 