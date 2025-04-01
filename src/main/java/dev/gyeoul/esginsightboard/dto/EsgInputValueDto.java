package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.EsgInputValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsgInputValueDto {
    private Long id;
    private Long indicatorId;
    private Long companyId;
    private Double numericValue;
    private String textValue;
    private String unit;
    private LocalDate reportingPeriodStart;
    private LocalDate reportingPeriodEnd;

    public static EsgInputValueDto fromEntity(EsgInputValue entity) {
        return EsgInputValueDto.builder()
                .id(entity.getId())
                .indicatorId(entity.getIndicator().getId())
                .companyId(entity.getCompany().getId())
                .numericValue(entity.getNumericValue())
                .textValue(entity.getTextValue())
                .unit(entity.getUnit())
                .reportingPeriodStart(entity.getReportingPeriodStart())
                .reportingPeriodEnd(entity.getReportingPeriodEnd())
                .build();
    }
}