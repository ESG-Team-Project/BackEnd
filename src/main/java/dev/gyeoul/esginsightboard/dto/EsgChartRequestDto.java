package dev.gyeoul.esginsightboard.dto;

import java.util.Map;

public class EsgChartRequestDto {
    // ESG 카테고리: "E", "S", "G"
    private String categoryCode;

    // 차트 제목
    private String chartTitle;

    // ESG 지표 이름 (ex. 원재료)
    private String indicatorName;

    // 지표별 상세 입력값 (key-value 구조)
    private Map<String, String> indicatorInputs;

    // 생성자
    public EsgChartRequestDto(String categoryCode, String chartTitle, String indicatorName, Map<String, String> indicatorInputs) {
        this.categoryCode = categoryCode;
        this.chartTitle = chartTitle;
        this.indicatorName = indicatorName;
        this.indicatorInputs = indicatorInputs;
    }

    // Getter
    public String getCategoryCode() { return categoryCode; }
    public String getChartTitle() { return chartTitle; }
    public String getIndicatorName() { return indicatorName; }
    public Map<String, String> getIndicatorInputs() { return indicatorInputs; }
}
