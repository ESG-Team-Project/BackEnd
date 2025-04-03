package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 보호
public class EsgInputValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 자동 증가 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private EsgIndicator indicator; // 연결된 ESG 지표

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 데이터를 입력한 회사

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company; // 데이터를 입력한 회사

    private Double numericValue; // 정량적 데이터 (예: 1000.0)

    @Column(length = 1000)
    private String textValue; // 정성적 데이터 (예: "재생 에너지 사용 증가")

    private String unit; // 데이터 단위 (예: "kWh", "tCO2eq")

    private LocalDate reportingPeriodStart; // 보고 기간 시작일
    private LocalDate reportingPeriodEnd; // 보고 기간 종료일
    
    private LocalDateTime createdAt;    // 엔티티 생성 일시
    private LocalDateTime upDateAt;     // 엔티티 수정 일시

    @Builder
    public EsgInputValue(EsgIndicator indicator, User user, Double numericValue, String textValue, String unit,
                         LocalDate reportingPeriodStart, LocalDate reportingPeriodEnd) {
        this.indicator = indicator;
        this.user = user;
        this.numericValue = numericValue;
        this.textValue = textValue;
        this.unit = unit;
        this.reportingPeriodStart = reportingPeriodStart;
        this.reportingPeriodEnd = reportingPeriodEnd;
        indicator.getInputValues().add(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.upDateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.upDateAt = LocalDateTime.now();
    }
}