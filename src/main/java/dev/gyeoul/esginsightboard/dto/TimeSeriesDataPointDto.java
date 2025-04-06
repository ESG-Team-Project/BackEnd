package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.TimeSeriesDataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시계열 데이터 포인트에 대한 DTO(Data Transfer Object)
 * <p>
 * 이 클래스는 GRI 데이터 항목의 시계열 데이터를 전송하는 데 사용됩니다.
 * 연도, 분기, 월 등의 시간 정보와 함께 값을 전송합니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesDataPointDto {

    /**
     * 데이터 포인트의 고유 식별자
     */
    private Long id;
    
    /**
     * 연관된 GRI 데이터 항목의 ID
     */
    private Long griDataItemId;
    
    /**
     * 연도
     */
    private Integer year;
    
    /**
     * 분기 (1-4)
     */
    private Integer quarter;
    
    /**
     * 월 (1-12)
     */
    private Integer month;
    
    /**
     * 데이터 값
     */
    private String value;
    
    /**
     * 단위
     */
    private String unit;
    
    /**
     * 추가 설명
     */
    private String notes;
    
    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;
    
    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;
    
    /**
     * TimeSeriesDataPoint 엔티티를 TimeSeriesDataPointDto로 변환
     *
     * @param entity 변환할 TimeSeriesDataPoint 엔티티
     * @return 변환된 TimeSeriesDataPointDto 객체
     */
    public static TimeSeriesDataPointDto fromEntity(TimeSeriesDataPoint entity) {
        TimeSeriesDataPointDtoBuilder builder = TimeSeriesDataPointDto.builder()
                .id(entity.getId())
                .year(entity.getYear())
                .quarter(entity.getQuarter())
                .month(entity.getMonth())
                .value(entity.getValue())
                .unit(entity.getUnit())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
        
        if (entity.getGriDataItem() != null) {
            builder.griDataItemId(entity.getGriDataItem().getId());
        }
        
        return builder.build();
    }
    
    /**
     * TimeSeriesDataPointDto를 TimeSeriesDataPoint 엔티티로 변환
     *
     * @return 생성된 TimeSeriesDataPoint 엔티티
     */
    public TimeSeriesDataPoint toEntity() {
        return TimeSeriesDataPoint.builder()
                .id(this.id)
                .year(this.year)
                .quarter(this.quarter)
                .month(this.month)
                .value(this.value)
                .unit(this.unit)
                .notes(this.notes)
                .build();
    }
} 