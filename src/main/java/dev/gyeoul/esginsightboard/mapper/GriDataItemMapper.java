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
        
        // 엔티티 생성 및 설정
        GriDataItem entity = new GriDataItem();
        
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        
        entity.setStandardCode(dto.getStandardCode());
        entity.setDisclosureCode(dto.getDisclosureCode());
        entity.setDisclosureTitle(dto.getDisclosureTitle());
        entity.setDisclosureValue(dto.getDisclosureValue());
        entity.setDescription(dto.getDescription());
        entity.setNumericValue(dto.getNumericValue());
        entity.setUnit(dto.getUnit());
        entity.setReportingPeriodStart(dto.getReportingPeriodStart());
        entity.setReportingPeriodEnd(dto.getReportingPeriodEnd());
        entity.setVerificationStatus(dto.getVerificationStatus());
        entity.setVerificationProvider(dto.getVerificationProvider());
        entity.setCategory(dto.getCategory());
        
        return entity;
    }
    
    /**
     * GriDataItemDto의 값으로 기존 GriDataItem 엔티티를 업데이트
     * 
     * @param dto 업데이트에 사용할 GriDataItemDto
     * @param entity 업데이트할 GriDataItem 엔티티
     */
    public void updateEntityFromDto(GriDataItemDto dto, GriDataItem entity) {
        if (dto == null || entity == null) {
            return;
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
        
        // 엔티티 필드 업데이트 (ID는 변경하지 않음)
        entity.setStandardCode(dto.getStandardCode());
        entity.setDisclosureCode(dto.getDisclosureCode());
        entity.setDisclosureTitle(dto.getDisclosureTitle());
        entity.setDisclosureValue(dto.getDisclosureValue());
        entity.setDescription(dto.getDescription());
        entity.setNumericValue(dto.getNumericValue());
        entity.setUnit(dto.getUnit());
        entity.setReportingPeriodStart(dto.getReportingPeriodStart());
        entity.setReportingPeriodEnd(dto.getReportingPeriodEnd());
        entity.setVerificationStatus(dto.getVerificationStatus());
        entity.setVerificationProvider(dto.getVerificationProvider());
        entity.setCategory(dto.getCategory());
    }
} 