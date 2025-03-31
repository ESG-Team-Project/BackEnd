package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "esg_indicators") // ESG 지표 테이블
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 보호
public class EsgIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 자동 증가 ID

    @Column(nullable = false, unique = true)
    private String code; // 지표 코드 (예: "GRI 302-1", "GRI 305-1")

    @Column(nullable = false)
    private String title; // 지표 제목 (예: "조직 내 에너지 소비", "온실가스 배출량")

    @Column(length = 2000)
    private String description; // 지표 설명 (선택 사항)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EsgCategory category; // 이 지표가 속한 ESG 카테고리

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EsgInputValue> inputValues = new ArrayList<>();

    public EsgIndicator(String code, String title, String description, EsgCategory category) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.category = category;
    }
}