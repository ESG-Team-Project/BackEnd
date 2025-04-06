package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.*;

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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
     * 대표자명
     * <p>
     * 회사 대표자(CEO)의 이름입니다.
     * </p>
     */
    @Column
    private String ceoName;

    /**
     * 회사 코드
     * <p>
     * 회사를 식별하는 고유 코드입니다. 주로 사용자가 회사 가입 시 사용합니다.
     * </p>
     */
    @Column
    private String companyCode;

    /**
     * 회사 대표 전화번호
     * <p>
     * 회사의 대표 전화번호입니다. 형식은 "XX-XXXX-XXXX"입니다.
     * </p>
     */
    @Column
    private String companyPhoneNumber;
    
    /**
     * 사업자등록번호
     * <p>
     * 회사의 공식 사업자등록번호입니다. 형식은 "XXX-XX-XXXXX"입니다.
     * </p>
     */
    @Column(length = 20)
    private String businessNumber;
    
    /**
     * 업종
     * <p>
     * 회사의 주요 업종 정보입니다.
     * </p>
     */
    @Column(length = 100)
    private String industry;
    
    /**
     * 섹터
     * <p>
     * 회사가 속한 시장 섹터 정보입니다.
     * </p>
     */
    @Column(length = 100)
    private String sector;
    
    /**
     * 종업원 수
     * <p>
     * 회사의 총 종업원 수입니다.
     * </p>
     */
    private Integer employeeCount;
    
    /**
     * 회사 설명
     * <p>
     * 회사에 대한 간략한 설명이나 소개글입니다.
     * </p>
     */
    @Column(length = 2000)
    private String description;
    
    /**
     * 이 회사에 속하는 GRI 데이터 항목 목록
     * <p>
     * 1:N 관계로, 하나의 회사는 여러 GRI 데이터 항목을 가질 수 있습니다.
     * </p>
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
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
     * GRI 데이터 항목을 이 회사에 추가하는 편의 메서드
     * <p>
     * 양방향 관계를 올바르게 설정합니다.
     * </p>
     * 
     * @param griDataItem 추가할 GRI 데이터 항목
     * @return 현재 Company 인스턴스 (메서드 체이닝용)
     */
    public Company addGriDataItem(GriDataItem griDataItem) {
        if (this.griDataItems == null) {
            this.griDataItems = new ArrayList<>();
        }
        this.griDataItems.add(griDataItem);
        griDataItem.setCompany(this);
        return this;
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
}