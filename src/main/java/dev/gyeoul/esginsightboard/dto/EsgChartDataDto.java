package dev.gyeoul.esginsightboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.Map;

@Getter
public class EsgChartDataDto {
    // ESG 카테고리: "E", "S", "G"
    @NotBlank(message = "카테고리 코드는 필수입니다.")
    private String categoryCode;
    // 차트 제목
    @NotBlank(message = "차트 제목은 필수입니다.")
    private String chartTitle;

    // ESG 지표 이름 (ex. 원재료)
    @NotBlank(message = "지표 이름은 필수입니다.")
    private String indicatorName;

    // 지표별 상세 입력값 (key-value 구조)
    @NotEmpty(message = "지표 입력값은 비어 있을 수 없습니다.")
    private Map<String, String> indicatorInputs;

    // 생성자
    public EsgChartDataDto(String categoryCode, String chartTitle, String indicatorName, Map<String, String> indicatorInputs) {
        this.categoryCode = categoryCode;
        this.chartTitle = chartTitle;
        this.indicatorName = indicatorName;
        this.indicatorInputs = indicatorInputs;
    }

}
