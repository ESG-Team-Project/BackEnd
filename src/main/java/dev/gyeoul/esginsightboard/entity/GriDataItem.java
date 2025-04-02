package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Table(name = "gri_data_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(nullable = false)
    private String standardCode;
    
    /**
     * GRI 공시 코드
     * <p>
     * 예: "302-1" (조직 내 에너지 소비), "305-1" (직접 온실가스 배출) 등
     * </p>
     */
    @Column(nullable = false)
    private String disclosureCode;
    
    /**
     * GRI 공시 항목 제목
     * <p>
     * 예: "조직 내 에너지 소비", "직접 온실가스 배출(Scope 1)" 등
     * </p>
     */
    @Column(nullable = false)
    private String disclosureTitle;
    
    /**
     * 공시 항목에 대한 텍스트 형태의 값
     * <p>
     * 숫자로 표현하기 어려운 정보를 기술하는 필드입니다.
     * 예: "재생 에너지 비중 증가 추세", "온실가스 감축 목표 초과 달성" 등
     * </p>
     */
    @Column(length = 1000)
    private String disclosureValue;
    
    /**
     * 데이터에 대한 추가 설명이나 맥락 정보
     * <p>
     * 데이터 수집 방법, 계산 방식, 특이 사항 등을 기술하는 필드입니다.
     * </p>
     */
    @Column(length = 2000)
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
    private LocalDate reportingPeriodStart;
    
    /**
     * 데이터 보고 기간의 종료일
     * <p>
     * 일반적으로 회계연도 종료일이나 분기 종료일입니다.
     * </p>
     */
    private LocalDate reportingPeriodEnd;
    
    /**
     * 데이터 검증 상태
     * <p>
     * 예: "미검증", "검증중", "검증완료", "검증실패" 등
     * </p>
     * @see VerificationStatus
     */
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
     * 이 데이터가 속한 회사 (다대일 관계)
     * <p>
     * 하나의 회사는 여러 GRI 데이터 항목을 가질 수 있습니다.
     * 이 관계는 지연 로딩(LAZY)으로 설정되어 있어, 필요할 때만 회사 정보를 불러옵니다.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    /**
     * 엔티티 생성 일시
     * <p>
     * 이 필드는 자동으로 설정되며 변경할 수 없습니다.
     * {@link #onCreate()} 메서드에서 설정됩니다.
     * </p>
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 엔티티 마지막 수정 일시
     * <p>
     * 이 필드는 자동으로 설정되며, 엔티티가 수정될 때마다 업데이트됩니다.
     * {@link #onUpdate()} 메서드에서 설정됩니다.
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * GriDataItem 엔티티 생성을 위한 빌더 패턴
     * <p>
     * 사용 예시:
     * <pre>
     * GriDataItem item = GriDataItem.builder()
     *     .standardCode("GRI 302")
     *     .disclosureCode("302-1")
     *     .disclosureTitle("조직 내 에너지 소비")
     *     .numericValue(10000.0)
     *     .unit("MWh")
     *     .category("E")
     *     .build();
     * </pre>
     * </p>
     */
    @Builder
    public GriDataItem(Long id, String standardCode, String disclosureCode, String disclosureTitle,
                       String disclosureValue, String description, Double numericValue, String unit,
                       LocalDate reportingPeriodStart, LocalDate reportingPeriodEnd,
                       String verificationStatus, String verificationProvider, String category,
                       Company company) {
        this.id = id;
        this.standardCode = standardCode;
        this.disclosureCode = disclosureCode;
        this.disclosureTitle = disclosureTitle;
        this.disclosureValue = disclosureValue;
        this.description = description;
        this.numericValue = numericValue;
        this.unit = unit;
        this.reportingPeriodStart = reportingPeriodStart;
        this.reportingPeriodEnd = reportingPeriodEnd;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.UNVERIFIED.getDisplayName();
        this.verificationProvider = verificationProvider;
        this.category = category;
        this.company = company;
    }
    
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
     * 회사 설정 메서드
     * <p>
     * 이 메서드는 GRI 데이터 항목을 특정 회사와 연결할 때 사용합니다.
     * </p>
     * 
     * @param company 연결할 회사 엔티티 (null일 수 있음)
     */
    public void setCompany(Company company) {
        this.company = company;
    }
    
    /**
     * 검증 상태 설정 메서드
     * <p>
     * 이 메서드는 GRI 데이터 항목의 검증 상태를 변경할 때 사용합니다.
     * </p>
     * 
     * @param status 설정할 검증 상태 열거형
     */
    public void setVerificationStatus(VerificationStatus status) {
        this.verificationStatus = status.getDisplayName();
    }
    
    /**
     * 검증 상태 직접 설정 메서드 (문자열)
     * <p>
     * 이 메서드는 문자열로 GRI 데이터 항목의 검증 상태를 변경할 때 사용합니다.
     * </p>
     * 
     * @param statusName 설정할 검증 상태 문자열
     */
    public void setVerificationStatusByName(String statusName) {
        this.verificationStatus = statusName;
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
} 