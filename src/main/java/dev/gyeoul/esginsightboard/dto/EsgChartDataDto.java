package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.EsgInputValue;
import dev.gyeoul.esginsightboard.entity.EsgIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsgChartDataDto {
    private Long inputValueId;
    private Long indicatorId;
    private Long companyId;
    private Double numericValue;
    private String textValue;
    private String unit;
    private LocalDate reportingPeriodStart;
    private LocalDate reportingPeriodEnd;
    private Long id;
    private String code;
    private String title;
    private String description;
    private String indicatorCode;
    private String indicatorTitle;
    private String indicatorDescription;
    private Long categoryId;

    public static EsgChartDataDto fromEntities(EsgInputValue inputValue, EsgIndicator indicator) {
        return EsgChartDataDto.builder()
                .inputValueId(inputValue.getId())
                .indicatorId(inputValue.getIndicator().getId())
                .companyId(inputValue.getCompany().getId())
                .numericValue(inputValue.getNumericValue())
                .textValue(inputValue.getTextValue())
                .unit(inputValue.getUnit())
                .reportingPeriodStart(inputValue.getReportingPeriodStart())
                .reportingPeriodEnd(inputValue.getReportingPeriodEnd())
                .id(indicator.getId())
                .code(indicator.getCode())
                .title(indicator.getTitle())
                .description(indicator.getDescription())
                .indicatorCode(indicator.getCode())
                .indicatorTitle(indicator.getTitle())
                .indicatorDescription(indicator.getDescription())
                .categoryId(indicator.getCategory().getId())
                .build();
    }
}