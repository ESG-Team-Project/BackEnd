package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 회사 정보를 저장하는 엔티티
 * <p>
 * 이 엔티티는 회사의 기본 정보와 메타데이터를 저장하며, GRI 데이터 항목과 1:N 관계를 가집니다.
 * 하나의 회사는 여러 GRI 데이터 항목을 가질 수 있습니다.
 * </p>
 * 
 * <p>
 * 주요 용도:
 * <ul>
 *   <li>ESG 데이터를 보고하는 회사의 기본 정보 저장</li>
 *   <li>회사별 ESG 데이터 그룹화 및 비교 분석 기반 제공</li>
 *   <li>사용자 소속 회사 정보 연결</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {
    
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
     * 회사명
     * <p>
     * 회사의 공식 이름입니다. 필수 필드입니다.
     * </p>
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 사업자등록번호
     * <p>
     * 회사의 공식 사업자등록번호입니다. 고유한 값이어야 합니다.
     * 형식: 000-00-00000
     * </p>
     */
    @Column(unique = true)
    private String businessNumber;
    
    /**
     * 업종
     * <p>
     * 회사의 주요 사업 영역 또는 업종을 나타냅니다.
     * 예: "제조업", "서비스업", "금융업" 등
     * </p>
     */
    @Column
    private String industry;
    
    /**
     * 산업 섹터
     * <p>
     * 회사가 속한 산업 섹터를 나타냅니다.
     * 예: "IT", "에너지", "헬스케어", "금융" 등
     * </p>
     */
    @Column
    private String sector;
    
    /**
     * 회사 설명
     * <p>
     * 회사에 대한 간략한 소개 또는 설명입니다.
     * 최대 1000자까지 저장할 수 있습니다.
     * </p>
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * 웹사이트 URL
     * <p>
     * 회사의 공식 웹사이트 URL입니다.
     * 예: "https://www.example.com"
     * </p>
     */
    @Column
    private String website;
    
    /**
     * 직원 수
     * <p>
     * 회사의 전체 직원 수를 나타냅니다.
     * </p>
     */
    @Column
    private Integer employeeCount;
    
    /**
     * 연간 매출액
     * <p>
     * 회사의 연간 매출액을 백만원 단위로
 저장합니다.
     * 예: 1000000은 10억원을 의미
     * </p>
     */
    @Column
    private Long annualRevenue;
    
    /**
     * 이 회사와 연결된 GRI 데이터 항목 목록 (양방향 관계)
     * <p>
     * 회사에 속한 모든 GRI 데이터 항목을 조회할 수 있습니다.
     * mappedBy 속성은 GriDataItem 엔티티의 company 필드가 이 관계의 주인임을 나타냅니다.
     * </p>
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GriDataItem> griDataItems = new ArrayList<>();
    
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
     * Company 엔티티 생성을 위한 빌더 패턴
     * <p>
     * 사용 예시:
     * <pre>
     * Company company = Company.builder()
     *     .name("한국ESG주식회사")
     *     .businessNumber("123-45-67890")
     *     .industry("제조업")
     *     .sector("에너지")
     *     .employeeCount(1000)
     *     .annualRevenue(50000L) // 5억원
     *     .build();
     * </pre>
     * </p>
     */
    @Builder
    public Company(Long id, String name, String businessNumber, String industry, String sector,
                  String description, String website, Integer employeeCount, Long annualRevenue) {
        this.id = id;
        this.name = name;
        this.businessNumber = businessNumber;
        this.industry = industry;
        this.sector = sector;
        this.description = description;
        this.website = website;
        this.employeeCount = employeeCount;
        this.annualRevenue = annualRevenue;
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
     * 회사 정보 업데이트
     * <p>
     * 회사의 기본 정보를 업데이트합니다. ID와 생성일은 변경되지 않습니다.
     * </p>
     * 
     * @param name 새 회사명 (null이면 변경 안 함)
     * @param industry 새 업종 (null이면 변경 안 함)
     * @param sector 새 섹터 (null이면 변경 안 함)
     * @param description 새 설명 (null이면 변경 안 함)
     * @param website 새 웹사이트 (null이면 변경 안 함)
     * @param employeeCount 새 직원 수 (null이면 변경 안 함)
     * @param annualRevenue 새 연간 매출액 (null이면 변경 안 함)
     */
    public void update(String name, String industry, String sector, String description,
                      String website, Integer employeeCount, Long annualRevenue) {
        if (name != null) this.name = name;
        if (industry != null) this.industry = industry;
        if (sector != null) this.sector = sector;
        if (description != null) this.description = description;
        if (website != null) this.website = website;
        if (employeeCount != null) this.employeeCount = employeeCount;
        if (annualRevenue != null) this.annualRevenue = annualRevenue;
    }
    
    /**
     * 사업자등록번호 업데이트
     * <p>
     * 사업자등록번호는 중요한 식별 정보이므로 별도의 메서드로 관리합니다.
     * </p>
     * 
     * @param businessNumber 새 사업자등록번호
     */
    public void updateBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }
    
    /**
     * GRI 데이터 항목 추가
     * <p>
     * 이 회사에 GRI 데이터 항목을 추가하고 양방향 관계를 설정합니다.
     * </p>
     * 
     * @param griDataItem 추가할 GRI 데이터 항목
     */
    public void addGriDataItem(GriDataItem griDataItem) {
        this.griDataItems.add(griDataItem);
        griDataItem.setCompany(this);
    }
    
    /**
     * GRI 데이터 항목 제거
     * <p>
     * 이 회사에서 GRI 데이터 항목을 제거하고 양방향 관계를 해제합니다.
     * </p>
     * 
     * @param griDataItem 제거할 GRI 데이터 항목
     */
    public void removeGriDataItem(GriDataItem griDataItem) {
        this.griDataItems.remove(griDataItem);
        griDataItem.setCompany(null);
    }
    
    /**
     * 두 회사 객체가 동일한지 비교
     * 
     * @param o 비교할 객체
     * @return 동일하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id);
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
     * 회사 정보를 문자열로 표현
     * 
     * @return 회사 정보 문자열
     */
    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", businessNumber='" + businessNumber + '\'' +
                ", industry='" + industry + '\'' +
                ", sector='" + sector + '\'' +
                '}';
    }
} 