package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "esg_indicator")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 보호
@AllArgsConstructor
public class EsgIndicator {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id; // 자동 증가 ID

    @Id
    @Column(nullable = false, unique = true)
    private String indicatorCode; // 지표 코드 (예: "GRI 302-1", "GRI 305-1")

    @Column(nullable = false)
    private String indicatorTitle; // 지표 제목 (예: "조직 내 에너지 소비", "온실가스 배출량")

    @Column(length = 2000)
    private String description; // 지표 설명 (선택 사항)

    // 이 데이터가 속한 카테고리 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EsgCategory category; // 이 지표가 속한 ESG 카테고리

    // 이 지표와 연결된 입력된 데이터 항목 목록
    // InputValue 엔티티의 Indicator 필드가 이 관계의 주인
    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EsgInputValue> inputValues = new ArrayList<>();

    @Builder
    public EsgIndicator(String indicatorCode, String indicatorTitle, String description, EsgCategory category) {
        this.indicatorCode = indicatorCode;
        this.indicatorTitle = indicatorTitle;
        this.description = description;
        this.category = category;
    }
}
