package dev.gyeoul.esginsightboard.mapper;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.TimeSeriesDataPointDto;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;

/**
 * GriDataItem 엔티티와 GriDataItemDto 간의 변환을 담당하는 매퍼 클래스
 */
@Slf4j
@Component
public class GriDataItemMapper {
    
    /**
     * GriDataItem 엔티티를 GriDataItemDto로 변환
     * 
     * @param entity 변환할 GriDataItem 엔티티
     * @return 변환된 GriDataItemDto
     */
    public GriDataItemDto toDto(GriDataItem entity) {
        if (entity == null) {
            return null;
        }
        
        GriDataItemDto dto = new GriDataItemDto();
        dto.setId(entity.getId());
        dto.setStandardCode(entity.getStandardCode());
        dto.setDisclosureCode(entity.getDisclosureCode());
        dto.setDisclosureTitle(entity.getDisclosureTitle());
        dto.setDisclosureValue(entity.getDisclosureValue());
        dto.setDescription(entity.getDescription());
        dto.setNumericValue(entity.getNumericValue());
        dto.setUnit(entity.getUnit());
        dto.setReportingPeriodStart(entity.getReportingPeriodStart());
        dto.setReportingPeriodEnd(entity.getReportingPeriodEnd());
        dto.setVerificationStatus(entity.getVerificationStatus());
        dto.setVerificationProvider(entity.getVerificationProvider());
        dto.setCategory(entity.getCategory());
        
        if (entity.getCompany() != null) {
            dto.setCompanyId(entity.getCompany().getId());
            dto.setCompanyName(entity.getCompany().getName());
        }
        
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // 시계열 데이터 처리
        try {
            if (dto.getDisclosureValue() != null && 
                dto.getDisclosureValue().startsWith("[") && 
                dto.getDisclosureValue().endsWith("]")) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                
                List<TimeSeriesDataPointDto> timeSeriesData = mapper.readValue(
                    dto.getDisclosureValue(),
                    new TypeReference<List<TimeSeriesDataPointDto>>() {}
                );
                dto.setTimeSeriesData(timeSeriesData);
            }
        } catch (Exception e) {
            log.error("시계열 데이터 파싱 오류: {}", e.getMessage());
        }
        
        return dto;
    }
    
    /**
     * GriDataItemDto를 GriDataItem 엔티티로 변환
     * 
     * @param dto 변환할 GriDataItemDto
     * @return 변환된 GriDataItem 엔티티
     */
    public GriDataItem toEntity(GriDataItemDto dto) {
        if (dto == null) {
            return null;
        }
        
        // 시계열 데이터가 있지만 disclosureValue에 반영되지 않은 경우 처리
        if (dto.getTimeSeriesData() != null && !dto.getTimeSeriesData().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                dto.setDisclosureValue(mapper.writeValueAsString(dto.getTimeSeriesData()));
                
                // 최신 값을 numericValue로 설정
                TimeSeriesDataPointDto latest = dto.getTimeSeriesData().stream()
                    .max(java.util.Comparator.comparing(TimeSeriesDataPointDto::getYear))
                    .orElse(null);
                
                if (latest != null) {
                    try {
                        dto.setNumericValue(Double.parseDouble(latest.getValue()));
                    } catch (NumberFormatException e) {
                        log.warn("시계열 데이터 값을 Double로 변환할 수 없습니다: {}", latest.getValue());
                        dto.setNumericValue(null);
                    }
                    dto.setUnit(latest.getUnit());
                }
            } catch (Exception e) {
                log.error("시계열 데이터 직렬화 오류: {}", e.getMessage());
            }
        }
        
        // Builder 패턴을 사용하여 엔티티 생성
        GriDataItem.GriDataItemBuilder builder = GriDataItem.builder()
            .standardCode(dto.getStandardCode())
            .disclosureCode(dto.getDisclosureCode())
            .disclosureTitle(dto.getDisclosureTitle())
            .disclosureValue(dto.getDisclosureValue())
            .description(dto.getDescription())
            .numericValue(dto.getNumericValue())
            .unit(dto.getUnit())
            .reportingPeriodStart(dto.getReportingPeriodStart())
            .reportingPeriodEnd(dto.getReportingPeriodEnd())
            .verificationStatus(dto.getVerificationStatus())
            .verificationProvider(dto.getVerificationProvider())
            .category(dto.getCategory());
            
        // ID가 있으면 설정
        if (dto.getId() != null) {
            builder.id(dto.getId());
        }
        
        return builder.build();
    }
    
    /**
     * GriDataItemDto의 값으로 기존 GriDataItem 엔티티를 업데이트
     * 
     * @param dto 업데이트에 사용할 GriDataItemDto
     * @param entity 업데이트할 GriDataItem 엔티티
     * @return 업데이트된 GriDataItem 엔티티
     */
    public GriDataItem updateEntityFromDto(GriDataItemDto dto, GriDataItem entity) {
        if (dto == null || entity == null) {
            return entity;
        }
        
        // 시계열 데이터가 있지만 disclosureValue에 반영되지 않은 경우 처리
        if (dto.getTimeSeriesData() != null && !dto.getTimeSeriesData().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                dto.setDisclosureValue(mapper.writeValueAsString(dto.getTimeSeriesData()));
                
                // 최신 값을 numericValue로 설정
                TimeSeriesDataPointDto latest = dto.getTimeSeriesData().stream()
                    .max(java.util.Comparator.comparing(TimeSeriesDataPointDto::getYear))
                    .orElse(null);
                
                if (latest != null) {
                    try {
                        dto.setNumericValue(Double.parseDouble(latest.getValue()));
                    } catch (NumberFormatException e) {
                        log.warn("시계열 데이터 값을 Double로 변환할 수 없습니다: {}", latest.getValue());
                        dto.setNumericValue(null);
                    }
                    dto.setUnit(latest.getUnit());
                }
            } catch (Exception e) {
                log.error("시계열 데이터 직렬화 오류: {}", e.getMessage());
            }
        }
        
        // 새 엔티티 생성하여 반환 (불변성 유지)
        return GriDataItem.builder()
            .id(entity.getId()) // 기존 ID 유지
            .standardCode(dto.getStandardCode())
            .disclosureCode(dto.getDisclosureCode())
            .disclosureTitle(dto.getDisclosureTitle())
            .disclosureValue(dto.getDisclosureValue())
            .description(dto.getDescription())
            .numericValue(dto.getNumericValue())
            .unit(dto.getUnit())
            .reportingPeriodStart(dto.getReportingPeriodStart())
            .reportingPeriodEnd(dto.getReportingPeriodEnd())
            .verificationStatus(dto.getVerificationStatus())
            .verificationProvider(dto.getVerificationProvider())
            .category(dto.getCategory())
            .company(entity.getCompany())  // 기존 관계 유지
            .createdAt(entity.getCreatedAt())  // 생성 시간 유지
            .dataType(entity.getDataType())  // 데이터 타입 유지
            .timeSeriesDataPoints(entity.getTimeSeriesDataPoints())  // 기존 시계열 데이터 유지
            .build();
    }
} 