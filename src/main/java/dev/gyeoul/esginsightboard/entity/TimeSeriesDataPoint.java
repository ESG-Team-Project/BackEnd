package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 시계열 데이터 포인트 엔티티
 * <p>
 * GRI 데이터 항목의 시계열 데이터를 저장하는 엔티티 클래스입니다.
 * 연도, 분기, 월 등의 시간 정보와 함께 값을 저장합니다.
 * </p>
 * 
 * <p>
 * 이 엔티티는 다음 관계를 갖고 있습니다:
 * <ul>
 *   <li>GriDataItem (N:1) - 여러 시계열 데이터 포인트가 하나의 GRI 데이터 항목에 속할 수 있습니다.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "time_series_data_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSeriesDataPoint {
    
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
     * 이 데이터 포인트가 속하는 GRI 데이터 항목
     * <p>
     * N:1 관계로, 여러 시계열 데이터 포인트가 하나의 GRI 데이터 항목에 속할 수 있습니다.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gri_data_item_id")
    private GriDataItem griDataItem;
    
    /**
     * 연도
     * <p>
     * 데이터가 해당하는 연도입니다.
     * </p>
     */
    private Integer year;
    
    /**
     * 분기 (1-4)
     * <p>
     * 데이터가 해당하는 분기입니다.
     * </p>
     */
    private Integer quarter;
    
    /**
     * 월 (1-12)
     * <p>
     * 데이터가 해당하는 월입니다.
     * </p>
     */
    private Integer month;
    
    /**
     * 데이터 값
     * <p>
     * 시계열 데이터의 값입니다.
     * </p>
     */
    private String value;
    
    /**
     * 단위
     * <p>
     * 데이터 값의 단위입니다.
     * </p>
     */
    private String unit;
    
    /**
     * 추가 설명
     * <p>
     * 데이터 포인트에 대한 추가 설명이나 비고 사항입니다.
     * </p>
     */
    private String notes;
    
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
     * TimeSeriesDataPoint 엔티티 생성을 위한 빌더 패턴
     */
    @Builder
    public TimeSeriesDataPoint(Long id, GriDataItem griDataItem, Integer year, Integer quarter, Integer month,
                              String value, String unit, String notes) {
        this.id = id;
        this.griDataItem = griDataItem;
        this.year = year;
        this.quarter = quarter;
        this.month = month;
        this.value = value;
        this.unit = unit;
        this.notes = notes;
    }
    
    /**
     * GRI 데이터 항목 설정
     * <p>
     * 이 시계열 데이터 포인트가 속하는 GRI 데이터 항목을 설정합니다.
     * </p>
     * 
     * @param griDataItem 설정할 GRI 데이터 항목
     */
    public void setGriDataItem(GriDataItem griDataItem) {
        this.griDataItem = griDataItem;
    }
    
    /**
     * 생성 시 자동으로 호출되는 메서드
     * <p>
     * 엔티티가 처음 저장될 때 createdAt과 updatedAt 필드를 현재 시간으로 설정합니다.
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * 업데이트 시 자동으로 호출되는 메서드
     * <p>
     * 엔티티가 업데이트될 때 updatedAt 필드를 현재 시간으로 설정합니다.
     * </p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * equals 메서드 오버라이드
     * <p>
     * 두 TimeSeriesDataPoint 객체가 같은지 비교합니다.
     * ID가 같으면 같은 객체로 간주합니다.
     * </p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSeriesDataPoint that = (TimeSeriesDataPoint) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * hashCode 메서드 오버라이드
     * <p>
     * TimeSeriesDataPoint 객체의 해시코드를 반환합니다.
     * ID를 기준으로 해시코드를 생성합니다.
     * </p>
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 