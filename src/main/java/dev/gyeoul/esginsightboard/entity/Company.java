package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

    @Column
    private String ceoName;     // 대표자명

    @Column
    private String companyCode; // 회사 코드

    @Column
    private String companyPhoneNumber; // 회사 전화번호


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

    @Builder
    public Company(Long id, String name, String ceoName, String companyCode, String companyPhoneNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.ceoName = ceoName;
        this.companyCode = companyCode;
        this.companyPhoneNumber = companyPhoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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