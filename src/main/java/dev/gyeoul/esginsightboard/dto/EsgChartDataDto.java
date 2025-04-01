package dev.gyeoul.esginsightboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Getter
public class EsgChartDataDto {
    // ESG 카테고리: "E", "S", "G"
    @NotBlank(message = "카테고리 코드는 필수입니다.")
    private String category;

    // 차트 제목
    @NotBlank(message = "차트 제목은 필수입니다.")
    private String chartTitle;

    // ESG 지표 이름 (ex. 원재료)
    @NotBlank(message = "지표 선택 필수입니다.")
    private String indicatorCode;

    // 지표별 상세 입력값 (key-value 구조)
    @NotEmpty(message = "지표 입력값은 비어 있을 수 없습니다.")
    private Map<String, String> indicatorInputs;

    private String unit; // 데이터 단위 (예: "kWh", "tCO2eq")

    private  Long companyId;

    // 생성자
    public EsgChartDataDto(String category, String chartTitle, String indicatorCode, Map<String, String> indicatorInputs, Long companyId) {
        String trimmedCode = category.trim().toUpperCase();

        // 직접 검증: E, S, G 중 하나인지 확인
        if (!Set.of("E", "S", "G").contains(trimmedCode)) {
            throw new IllegalArgumentException("카테고리 코드는 E, S, G 중 하나여야 합니다.");
        }
        this.category = category;
        this.chartTitle = chartTitle;
        this.indicatorCode = indicatorCode;
        this.indicatorInputs = indicatorInputs;
        this.unit = indicatorInputs.get("unit");
        this.companyId = companyId;
    }
}
