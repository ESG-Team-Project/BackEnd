package dev.gyeoul.esginsightboard.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GRI(Global Reporting Initiative) 데이터 항목 엔티티
 * <p>
 * ESG 보고서의 GRI 표준에 따른 공시 데이터를 저장하는 엔티티 클래스입니다.
 * 
 * GRI 표준은 기업의 ESG(환경, 사회, 지배구조) 성과를 보고하기 위한 국제 표준이며,
 * 표준 코드(GRI 302 등)와 공시 코드(302-1 등)로 구성됩니다.
 * </p>
 * 
 * <p>
 * 이 엔티티는 다음 관계를 갖고 있습니다:
 * <ul>
 *   <li>Company (N:1) - 여러 GRI 데이터 항목이 하나의 회사에 속할 수 있습니다.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 주요 용도:
 * <ul>
 *   <li>에너지 사용량, 온실가스 배출량 등의 환경 데이터 저장</li>
 *   <li>고용, 다양성, 안전보건 등의 사회 데이터 저장</li>
 *   <li>경제성과, 반부패 등의 지배구조 데이터 저장</li>
 * </ul>
 * </p>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "gri_data_items", 
       indexes = {
           @Index(name = "idx_gri_company_id", columnList = "company_id"),
           @Index(name = "idx_gri_standard_code", columnList = "standard_code"),
           @Index(name = "idx_gri_disclosure_code", columnList = "disclosure_code"),
           @Index(name = "idx_gri_category", columnList = "category"),
           @Index(name = "idx_gri_reporting_period", columnList = "reporting_period_start, reporting_period_end"),
           @Index(name = "idx_gri_verification_status", columnList = "verification_status"),
           @Index(name = "idx_gri_company_category", columnList = "company_id, category")
       })
public class GriDataItem {
    
    /**
     * 검증 상태 열거형 (enum)
     * <p>
     * GRI 데이터 항목의 검증 상태를 나타내는 열거형 타입입니다.
     * </p>
     */
    public enum VerificationStatus {
        /** 미검증 상태 */
        UNVERIFIED("미검증"),
        
        /** 검증 진행 중 상태 */
        IN_PROGRESS("검증중"),
        
        /** 검증 완료 상태 */
        VERIFIED("검증완료"),
        
        /** 검증 실패 상태 */
        FAILED("검증실패");
        
        private final String displayName;
        
        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * 표시 이름으로 열거형 값을 찾습니다.
         * 
         * @param displayName 표시 이름
         * @return 해당하는 VerificationStatus 또는 없으면 UNVERIFIED
         */
        public static VerificationStatus fromDisplayName(String displayName) {
            for (VerificationStatus status : values()) {
                if (status.displayName.equals(displayName)) {
                    return status;
                }
            }
            return UNVERIFIED;
        }
    }
    
    /**
     * 데이터 유형 열거형 (enum)
     * <p>
     * GRI 데이터 항목의 데이터 유형을 나타내는 열거형 타입입니다.
     * </p>
     */
    public enum DataType {
        /** 시계열 데이터 */
        TIMESERIES,
        
        /** 텍스트 데이터 */
        TEXT,
        
        /** 숫자 데이터 */
        NUMERIC
    }
    
    /**
     * 고유 식별자 (기본 키)
     * <p>
     * 자동 생성되는 ID로, 데이터베이스에서 이 항목을 고유하게 식별합니다.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * GRI 표준 코드
     * <p>
     * 예: "GRI 302" (에너지), "GRI 305" (배출) 등
     * </p>
     */
    @Column(name = "standard_code", nullable = false)
    private String standardCode;
    
    /**
     * GRI 공시 코드
     * <p>
     * 예: "302-1" (조직 내 에너지 소비), "305-1" (직접 온실가스 배출) 등
     * </p>
     */
    @Column(name = "disclosure_code", nullable = false)
    private String disclosureCode;
    
    /**
     * GRI 공시 항목 제목
     * <p>
     * 예: "조직 내 에너지 소비", "직접 온실가스 배출(Scope 1)" 등
     * </p>
     */
    @Column(name = "disclosure_title", nullable = false)
    private String disclosureTitle;
    
    /**
     * 공시 항목에 대한 값
     * <p>
     * 공시 항목에 대한 응답 또는 값을 저장합니다.
     * 텍스트 형태, 수치 값, 또는 시계열 데이터의 JSON 문자열 등이 저장될 수 있습니다.
     * </p>
     */
    @Column(name = "disclosure_value", columnDefinition = "TEXT")
    private String disclosureValue;
    
    /**
     * 공시 항목에 대한 텍스트 형태의 값
     * <p>
     * 숫자로 표현하기 어려운 정보를 기술하는 필드입니다.
     * 예: "재생 에너지 비중 증가 추세", "온실가스 감축 목표 초과 달성" 등
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 정량적 수치 값
     * <p>
     * 에너지 사용량, 온실가스 배출량 등의 수치를 저장하는 필드입니다.
     * 이 값은 unit 필드와 함께 사용됩니다.
     * </p>
     */
    private Double numericValue;
    
    /**
     * 수치 값의 단위
     * <p>
     * 예: "kWh", "tCO2eq", "명", "%" 등
     * </p>
     */
    private String unit;
    
    /**
     * 데이터 보고 기간의 시작일
     * <p>
     * 일반적으로 회계연도 시작일이나 분기 시작일입니다.
     * </p>
     */
    @Column(name = "reporting_period_start")
    private LocalDate reportingPeriodStart;
    
    /**
     * 데이터 보고 기간의 종료일
     * <p>
     * 일반적으로 회계연도 종료일이나 분기 종료일입니다.
     * </p>
     */
    @Column(name = "reporting_period_end")
    private LocalDate reportingPeriodEnd;
    
    /**
     * 데이터 검증 상태
     * <p>
     * 예: "미검증", "검증중", "검증완료", "검증실패" 등
     * </p>
     * @see VerificationStatus
     */
    @Column(name = "verification_status")
    private String verificationStatus;
    
    /**
     * 검증을 수행한 기관 또는 제공자
     * <p>
     * 예: "한국품질재단", "DNV GL", "KPMG" 등
     * </p>
     */
    private String verificationProvider;
    
    /**
     * ESG 카테고리 (E: 환경, S: 사회, G: 지배구조)
     * <p>
     * 이 필드는 GRI 표준 코드를 기반으로 자동으로 결정됩니다.
     * </p>
     */
    @Column(nullable = false)
    private String category;
    
    /**
     * 이 데이터 항목이 속하는 회사
     * <p>
     * N:1 관계로, 여러 GRI 데이터 항목이 하나의 회사에 속할 수 있습니다.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private Company company;
    
    /**
     * 엔티티 생성 일시
     * <p>
     * 이 필드는 자동으로 설정되며 변경할 수 없습니다.
     * {@link #onCreate()} 메서드에서 설정됩니다.
     * </p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 엔티티 마지막 수정 일시
     * <p>
     * 이 필드는 자동으로 설정되며, 엔티티가 수정될 때마다 업데이트됩니다.
     * {@link #onUpdate()} 메서드에서 설정됩니다.
     * </p>
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 데이터 유형
     * <p>
     * 데이터의 형식을 지정합니다. 시계열, 텍스트, 숫자 중 하나입니다.
     * </p>
     * @see DataType
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DataType dataType = DataType.TEXT;
    
    /**
     * 시계열 데이터 포인트 목록
     * <p>
     * 데이터 유형이 TIMESERIES인 경우, 이 필드에 시계열 데이터를 저장합니다.
     * </p>
     */
    @OneToMany(mappedBy = "griDataItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeSeriesDataPoint> timeSeriesDataPoints = new ArrayList<>();
    
    /**
     * 엔티티 생성 시 호출되어 생성 시간과 수정 시간을 현재 시간으로 설정
     * <p>
     * 이 메서드는 {@link PrePersist} 어노테이션으로 인해 엔티티가 데이터베이스에
     * 저장되기 전에 자동으로 호출됩니다.
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 시 호출되어 수정 시간을 현재 시간으로 업데이트
     * <p>
     * 이 메서드는 {@link PreUpdate} 어노테이션으로 인해 엔티티가 데이터베이스에서
     * 업데이트되기 전에 자동으로 호출됩니다.
     * </p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 현재 검증 상태를 열거형으로 반환
     * 
     * @return 현재 검증 상태에 해당하는 열거형 값
     */
    public VerificationStatus getVerificationStatusEnum() {
        return VerificationStatus.fromDisplayName(this.verificationStatus);
    }
    
    /**
     * 시계열 데이터 포인트 추가
     * <p>
     * 이 GRI 데이터 항목에 시계열 데이터 포인트를 추가합니다.
     * </p>
     * 
     * @param dataPoint 추가할 시계열 데이터 포인트
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem addTimeSeriesDataPoint(TimeSeriesDataPoint dataPoint) {
        if (timeSeriesDataPoints == null) {
            timeSeriesDataPoints = new ArrayList<>();
        }
        timeSeriesDataPoints.add(dataPoint);
        dataPoint.setGriDataItem(this);
        return this;
    }
    
    /**
     * 시계열 데이터 포인트 제거
     * <p>
     * 이 GRI 데이터 항목에서 시계열 데이터 포인트를 제거합니다.
     * </p>
     * 
     * @param dataPoint 제거할 시계열 데이터 포인트
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem removeTimeSeriesDataPoint(TimeSeriesDataPoint dataPoint) {
        timeSeriesDataPoints.remove(dataPoint);
        dataPoint.setGriDataItem(null);
        return this;
    }
    
    /**
     * 검증 상태 설정 메서드
     * <p>
     * 이 메서드는 GRI 데이터 항목의 검증 상태를 변경할 때 사용합니다.
     * </p>
     * 
     * @param status 설정할 검증 상태 열거형
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem setVerificationStatus(VerificationStatus status) {
        this.verificationStatus = status.getDisplayName();
        return this;
    }
    
    /**
     * 검증 상태 설정 메서드 (문자열 버전)
     * <p>
     * 이 메서드는 문자열로 GRI 데이터 항목의 검증 상태를 변경할 때 사용합니다.
     * </p>
     * 
     * @param statusDisplayName 설정할 검증 상태 문자열
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem setVerificationStatus(String statusDisplayName) {
        this.verificationStatus = statusDisplayName;
        return this;
    }
    
    /**
     * 회사 설정 메서드
     * <p>
     * 이 메서드는 GRI 데이터 항목이 속한 회사를 설정할 때 사용합니다.
     * </p>
     * 
     * @param company 설정할 회사 엔티티
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem setCompany(Company company) {
        this.company = company;
        return this;
    }
    
    /**
     * 데이터 유형 설정
     * <p>
     * GRI 데이터 항목의 데이터 유형을 설정합니다.
     * </p>
     * 
     * @param dataType 설정할 데이터 유형
     * @return 현재 GriDataItem 인스턴스 (메서드 체이닝용)
     */
    public GriDataItem setDataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }
    
    /**
     * 표준 코드를 설정합니다.
     * 
     * @param standardCode 설정할 표준 코드
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setStandardCode(String standardCode) {
        this.standardCode = standardCode;
        return this;
    }

    /**
     * 공시 코드를 설정합니다.
     * 
     * @param disclosureCode 설정할 공시 코드
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setDisclosureCode(String disclosureCode) {
        this.disclosureCode = disclosureCode;
        return this;
    }

    /**
     * 공시 제목을 설정합니다.
     * 
     * @param disclosureTitle 설정할 공시 제목
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setDisclosureTitle(String disclosureTitle) {
        this.disclosureTitle = disclosureTitle;
        return this;
    }

    /**
     * 공시 값을 설정합니다.
     * 
     * @param disclosureValue 설정할 공시 값
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setDisclosureValue(String disclosureValue) {
        this.disclosureValue = disclosureValue;
        return this;
    }

    /**
     * 설명을 설정합니다.
     * 
     * @param description 설정할 설명
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 수치 값을 설정합니다.
     * 
     * @param numericValue 설정할 수치 값
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setNumericValue(Double numericValue) {
        this.numericValue = numericValue;
        return this;
    }

    /**
     * 단위를 설정합니다.
     * 
     * @param unit 설정할 단위
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * 보고 기간 시작일을 설정합니다.
     * 
     * @param reportingPeriodStart 설정할 보고 기간 시작일
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setReportingPeriodStart(LocalDate reportingPeriodStart) {
        this.reportingPeriodStart = reportingPeriodStart;
        return this;
    }

    /**
     * 보고 기간 종료일을 설정합니다.
     * 
     * @param reportingPeriodEnd 설정할 보고 기간 종료일
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setReportingPeriodEnd(LocalDate reportingPeriodEnd) {
        this.reportingPeriodEnd = reportingPeriodEnd;
        return this;
    }

    /**
     * 검증 제공자를 설정합니다.
     * 
     * @param verificationProvider 설정할 검증 제공자
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setVerificationProvider(String verificationProvider) {
        this.verificationProvider = verificationProvider;
        return this;
    }

    /**
     * 카테고리를 설정합니다.
     * 
     * @param category 설정할 카테고리
     * @return this (메소드 체이닝용)
     */
    public GriDataItem setCategory(String category) {
        this.category = category;
        return this;
    }
    
    /**
     * 두 GRI 데이터 항목이 동일한지 비교
     * 
     * @param o 비교할 객체
     * @return 동일하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GriDataItem that = (GriDataItem) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * 해시 코드 계산
     * 
     * @return 이 객체의 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * 이 항목이 특정 카테고리에 속하는지 확인
     * 
     * @param categoryToCheck 확인할 카테고리
     * @return 해당 카테고리에 속하면 true, 아니면 false
     */
    public boolean isInCategory(String categoryToCheck) {
        return category != null && category.equalsIgnoreCase(categoryToCheck);
    }

    /**
     * 이 항목이 특정 보고 기간에 속하는지 확인
     * 
     * @param startDate 확인할 시작일
     * @param endDate 확인할 종료일
     * @return 해당 기간에 속하면 true, 아니면 false
     */
    public boolean isInReportingPeriod(LocalDate startDate, LocalDate endDate) {
        if (reportingPeriodStart == null || reportingPeriodEnd == null) {
            return false;
        }
        
        return !reportingPeriodEnd.isBefore(startDate) && !reportingPeriodStart.isAfter(endDate);
    }

    /**
     * 이 항목이 특정 검증 상태인지 확인
     * 
     * @param status 확인할 검증 상태
     * @return 해당 상태이면 true, 아니면 false
     */
    public boolean hasVerificationStatus(String status) {
        return verificationStatus != null && verificationStatus.equalsIgnoreCase(status);
    }

    /**
     * GRI 데이터 항목 복제 (새 객체 생성)
     * 
     * @return 현재 객체의 복제본
     */
    public GriDataItem copy() {
        return GriDataItem.builder()
            .standardCode(this.standardCode)
            .disclosureCode(this.disclosureCode)
            .category(this.category)
            .disclosureTitle(this.disclosureTitle)
            .disclosureValue(this.disclosureValue)
            .description(this.description)
            .reportingPeriodStart(this.reportingPeriodStart)
            .reportingPeriodEnd(this.reportingPeriodEnd)
            .verificationStatus(this.verificationStatus)
            .verificationProvider(this.verificationProvider)
            .company(this.company)
            .dataType(this.dataType)
            .build();
    }
} 