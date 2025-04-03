package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chart_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChartData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 차트 제목 (공백 불가)

    @Column(nullable = false, length = 500)
    private String description; // 차트 설명 (공백 불가)

    @Column(nullable = false, length = 1)
    private String category; // ESG 카테고리 (E, S, G)

    @Column(nullable = false, length = 100)
    private String indicator; // 세부 지표

    @Column(nullable = false, length = 20)
    private String chartType; // 차트 유형 (Bar, Line, Area, Pie, Donut)

    @Column(nullable = false)
    private Integer chartGrid; // 차트 칸 (1~4)

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String data; // 사용자 입력 데이터 (JSON 저장)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ChartData(String title, String description, String category, String indicator,
                     String chartType, Integer chartGrid, String data) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.indicator = indicator;
        this.chartType = chartType;
        this.chartGrid = chartGrid;
        this.data = data;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}