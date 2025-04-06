package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * GRI 데이터 항목 검색 조건을 위한 DTO 클래스
 * <p>
 * 이 클래스는 GRI 데이터 항목을 다양한 조건으로 필터링하기 위한 검색 조건을 정의합니다.
 * 모든 필드는 선택적이며, 지정된 필드만 검색 조건으로 사용됩니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GRI 데이터 항목 검색 조건")
public class GriDataSearchCriteria {

    @Schema(description = "카테고리 (Environmental, Social, Governance, Economic, 일반)", example = "Environmental")
    private String category;

    @Schema(description = "GRI 표준 코드", example = "GRI 302")
    @Pattern(regexp = "^GRI\\s\\d{3}$", message = "GRI 표준 코드 형식은 'GRI 숫자'여야 합니다 (예: GRI 302)")
    private String standardCode;

    @Schema(description = "GRI 공시 코드", example = "302-1")
    @Pattern(regexp = "^\\d{3}-\\d{1,2}$", message = "GRI 공시 코드 형식은 '숫자-숫자'여야 합니다 (예: 302-1)")
    private String disclosureCode;

    @Schema(description = "보고 기간 시작일", example = "2023-01-01")
    private LocalDate reportingPeriodStart;

    @Schema(description = "보고 기간 종료일", example = "2023-12-31")
    private LocalDate reportingPeriodEnd;

    @Schema(description = "검증 상태 (예: 미검증, 검증중, 검증완료)", example = "검증완료")
    private String verificationStatus;

    @Schema(description = "회사 ID", example = "1")
    private Long companyId;

    @Schema(description = "키워드 검색 (제목, 설명에서 검색)", example = "에너지")
    private String keyword;

    @Schema(description = "정렬 기준 (형식: 속성,정렬방향)", example = "disclosureCode,asc")
    private String sort;
} 