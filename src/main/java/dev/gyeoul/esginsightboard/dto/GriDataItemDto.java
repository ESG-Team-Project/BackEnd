package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.entity.TimeSeriesDataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GRI(Global Reporting Initiative) 데이터 항목에 대한 DTO(Data Transfer Object)
 * <p>
 * 이 클래스는 ESG(환경, 사회, 지배구조) 데이터 중 GRI 표준에 따른 데이터 항목을 전송하는 데 사용됩니다.
 * GRI는 전 세계적으로 사용되는 지속가능성 보고 표준으로, 기업의 ESG 성과를 공개하는 지침을 제공합니다.
 * </p>
 * 
 * <p>
 * 이 DTO는 다음과 같은 용도로 사용됩니다:
 * <ul>
 *   <li>API 응답으로 GRI 데이터 항목 정보 제공</li>
 *   <li>클라이언트의 GRI 데이터 항목 생성/수정 요청 처리</li>
 *   <li>엔티티와 클라이언트 간 데이터 변환 매개체</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GriDataItemDto {

    /**
     * ESG 환경(Environmental) 카테고리 코드
     * <p>
     * 온실가스 배출, 에너지 사용, 폐기물 관리 등 환경 관련 지표에 사용됩니다.
     * GRI 300 시리즈(301-308)에 해당합니다.
     * </p>
     */
    public static final String CATEGORY_ENVIRONMENTAL = "E";
    
    /**
     * ESG 사회(Social) 카테고리 코드
     * <p>
     * 인권, 노동관행, 산업안전보건, 다양성, 지역사회 등 사회 관련 지표에 사용됩니다.
     * GRI 400 시리즈(401-419)에 해당합니다.
     * </p>
     */
    public static final String CATEGORY_SOCIAL = "S";
    
    /**
     * ESG 지배구조(Governance) 카테고리 코드
     * <p>
     * 기업윤리, 반부패, 이사회 구성, 위험관리, 정보공개 등 지배구조 관련 지표에 사용됩니다.
     * GRI 200 시리즈(201-207)에 해당합니다.
     * </p>
     */
    public static final String CATEGORY_GOVERNANCE = "G";

    /**
     * 데이터 항목의 고유 식별자
     * <p>
     * 데이터베이스에서 자동 생성되는 기본 키입니다.
     * </p>
     */
    private Long id;
    
    /**
     * GRI 표준 코드
     * <p>
     * GRI 표준의 번호를 나타냅니다(예: "GRI 302" - 에너지).
     * 필수 필드입니다.
     * </p>
     */
    private String standardCode;
    
    /**
     * GRI 공시 코드
     * <p>
     * 표준 내 특정 공시 항목의 코드입니다(예: "302-1" - 조직 내 에너지 소비).
     * </p>
     */
    private String disclosureCode;
    
    /**
     * 공시 항목 제목
     * <p>
     * 공시 항목의 제목 또는 요약입니다(예: "조직 내 에너지 소비").
     * 필수 필드입니다.
     * </p>
     */
    private String disclosureTitle;
    
    /**
     * 공시 항목 값(텍스트)
     * <p>
     * 정량적으로 표현하기 어려운 공시 항목의 서술형 값입니다.
     * 예: 환경 정책 설명, 인권 정책 수립 여부 등
     * </p>
     */
    private String disclosureValue;
    
    /**
     * 공시 항목 값(수치)
     * <p>
     * 정량적으로 측정 가능한 공시 항목의 수치 값입니다.
     * 예: 에너지 사용량, 온실가스 배출량, 용수 사용량 등
     * </p>
     */
    private Double numericValue;
    
    /**
     * 수치 값의 단위
     * <p>
     * numericValue의 측정 단위입니다.
     * 예: "MWh"(에너지), "tCO2e"(온실가스), "톤"(폐기물) 등
     * </p>
     */
    private String unit;
    
    /**
     * 보고 기간 시작일
     * <p>
     * 데이터가 수집된 기간의 시작일입니다.
     * 예: 회계연도 시작일, 분기 시작일 등
     * </p>
     */
    private LocalDate reportingPeriodStart;
    
    /**
     * 보고 기간 종료일
     * <p>
     * 데이터가 수집된 기간의 종료일입니다.
     * 예: 회계연도 종료일, 분기 종료일 등
     * </p>
     */
    private LocalDate reportingPeriodEnd;
    
    /**
     * 검증 상태
     * <p>
     * 데이터의 외부 검증 상태를 나타냅니다.
     * 가능한 값: "검증됨", "미검증", "검증 진행 중" 등
     * </p>
     */
    private String verificationStatus;
    
    /**
     * 검증 제공자
     * <p>
     * 데이터를 검증한 외부 기관의 이름입니다.
     * 예: "한국품질재단", "DNV-GL", "KPMG" 등
     * </p>
     */
    private String verificationProvider;
    
    /**
     * ESG 카테고리
     * <p>
     * 데이터 항목이 속한 ESG 카테고리입니다:
     * <ul>
     *   <li>"E" - 환경(Environmental)</li>
     *   <li>"S" - 사회(Social)</li>
     *   <li>"G" - 지배구조(Governance)</li>
     * </ul>
     * </p>
     */
    private String category;
    
    /**
     * 회사 ID
     * <p>
     * 이 데이터 항목을 보고한 회사의 고유 식별자입니다.
     * </p>
     */
    private Long companyId;
    
    /**
     * 회사명
     * <p>
     * 이 데이터 항목을 보고한 회사의 이름입니다.
     * 응답 시 편의성을 위해 포함됩니다.
     * </p>
     */
    private String companyName;
    
    /**
     * 추가 설명
     * <p>
     * 데이터 항목에 대한 추가 설명이나 비고 사항입니다.
     * </p>
     */
    private String description;
    
    /**
     * 데이터 생성 일시
     * <p>
     * 이 데이터 항목이 시스템에 처음 등록된 일시입니다.
     * </p>
     */
    private LocalDateTime createdAt;
    
    /**
     * 데이터 수정 일시
     * <p>
     * 이 데이터 항목이 마지막으로 수정된 일시입니다.
     * </p>
     */
    private LocalDateTime updatedAt;

    /**
     * 데이터 유형
     * <p>
     * 이 데이터 항목의 형식을 나타냅니다. TIMESERIES, TEXT, NUMERIC 중 하나입니다.
     * </p>
     */
    private String dataType;
    
    /**
     * 시계열 데이터 포인트 목록
     * <p>
     * 데이터 유형이 TIMESERIES인 경우, 이 필드에 시계열 데이터가 포함됩니다.
     * </p>
     */
    private List<TimeSeriesDataPointDto> timeSeriesData;

    /**
     * GriDataItem 엔티티를 GriDataItemDto로 변환
     * <p>
     * 엔티티의 모든 필드를 DTO로 복사하고, 회사 정보가 있으면 회사 관련 필드도 설정합니다.
     * </p>
     * 
     * <p>
     * 사용 예시:
     * <pre>
     * GriDataItem entity = griDataItemRepository.findById(1L).orElseThrow();
     * GriDataItemDto dto = GriDataItemDto.fromEntity(entity);
     * </pre>
     * </p>
     *
     * @param entity 변환할 GriDataItem 엔티티
     * @return 변환된 GriDataItemDto 객체
     */
    public static GriDataItemDto fromEntity(GriDataItem entity) {
        GriDataItemDtoBuilder builder = GriDataItemDto.builder()
                .id(entity.getId())
                .standardCode(entity.getStandardCode())
                .disclosureCode(entity.getDisclosureCode())
                .disclosureTitle(entity.getDisclosureTitle())
                .disclosureValue(entity.getDisclosureValue())
                .numericValue(entity.getNumericValue())
                .unit(entity.getUnit())
                .reportingPeriodStart(entity.getReportingPeriodStart())
                .reportingPeriodEnd(entity.getReportingPeriodEnd())
                .verificationStatus(entity.getVerificationStatus())
                .verificationProvider(entity.getVerificationProvider())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .dataType(entity.getDataType() != null ? entity.getDataType().name() : null);

        if (entity.getCompany() != null) {
            builder.companyId(entity.getCompany().getId())
                   .companyName(entity.getCompany().getName());
        }
        
        // 시계열 데이터가 있는 경우 변환
        if (entity.getTimeSeriesDataPoints() != null && !entity.getTimeSeriesDataPoints().isEmpty()) {
            List<TimeSeriesDataPointDto> timeSeriesDataList = new ArrayList<>();
            for (TimeSeriesDataPoint dataPoint : entity.getTimeSeriesDataPoints()) {
                timeSeriesDataList.add(TimeSeriesDataPointDto.fromEntity(dataPoint));
            }
            builder.timeSeriesData(timeSeriesDataList);
        }

        return builder.build();
    }

    /**
     * GriDataItemDto를 GriDataItem 엔티티로 변환
     * <p>
     * DTO의 필드를 새 엔티티에 복사합니다. ID가 있으면 업데이트로 간주하고,
     * 없으면 새 엔티티 생성으로 간주합니다.
     * </p>
     * 
     * <p>
     * 사용 예시:
     * <pre>
     * GriDataItemDto dto = new GriDataItemDto();
     * // dto 필드 설정...
     * GriDataItem entity = dto.toEntity();
     * griDataItemRepository.save(entity);
     * </pre>
     * </p>
     *
     * @return 생성된 GriDataItem 엔티티
     */
    public GriDataItem toEntity() {
        GriDataItem entity = GriDataItem.builder()
                .id(this.id)  // null이면 새 항목, 값이 있으면 업데이트
                .standardCode(this.standardCode)
                .disclosureCode(this.disclosureCode)
                .disclosureTitle(this.disclosureTitle)
                .disclosureValue(this.disclosureValue)
                .numericValue(this.numericValue)
                .unit(this.unit)
                .reportingPeriodStart(this.reportingPeriodStart)
                .reportingPeriodEnd(this.reportingPeriodEnd)
                .verificationStatus(this.verificationStatus)
                .verificationProvider(this.verificationProvider)
                .category(this.category)
                .description(this.description)
                .dataType(this.dataType != null ? GriDataItem.DataType.valueOf(this.dataType) : null)
                .build();
        
        // 시계열 데이터가 있는 경우 추가
        if (this.timeSeriesData != null && !this.timeSeriesData.isEmpty()) {
            for (TimeSeriesDataPointDto dataPointDto : this.timeSeriesData) {
                TimeSeriesDataPoint dataPoint = dataPointDto.toEntity();
                entity.addTimeSeriesDataPoint(dataPoint);
            }
        }
        
        return entity;
    }
    
    /**
     * 현재 DTO 객체가 유효한지 검사
     * <p>
     * 필수 필드가 모두 설정되었는지 확인합니다.
     * </p>
     *
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean isValid() {
        return standardCode != null && !standardCode.trim().isEmpty() &&
               disclosureTitle != null && !disclosureTitle.trim().isEmpty();
    }
    
    /**
     * DTO 객체 생성을 위한 빌더 패턴 사용 예시
     * <p>
     * 아래는 빌더 패턴을 사용하여 GriDataItemDto 객체를 생성하는 예시입니다:
     * </p>
     * 
     * <pre>
     * GriDataItemDto energyConsumptionDto = GriDataItemDto.builder()
     *     .standardCode("GRI 302")
     *     .disclosureCode("302-1")
     *     .disclosureTitle("조직 내 에너지 소비")
     *     .numericValue(15000.0)
     *     .unit("MWh")
     *     .reportingPeriodStart(LocalDate.of(2023, 1, 1))
     *     .reportingPeriodEnd(LocalDate.of(2023, 12, 31))
     *     .category(GriDataItemDto.CATEGORY_ENVIRONMENTAL)
     *     .verificationStatus("검증됨")
     *     .companyId(1L)
     *     .build();
     * </pre>
     */
} 