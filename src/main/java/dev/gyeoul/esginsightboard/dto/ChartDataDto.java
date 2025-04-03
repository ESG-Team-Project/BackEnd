package dev.gyeoul.esginsightboard.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gyeoul.esginsightboard.entity.ChartData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ChartDataDto {
    private String title;
    private String description;
    private String category;
    private String indicator;
    private Integer chartGrid;
    private List<Map<String, Object>> data;
    private String chartType;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ 엔티티 → DTO 변환
    public static ChartDataDto fromEntity(ChartData chartData) {
        ChartDataDto dto = new ChartDataDto();
        dto.setTitle(chartData.getTitle());
        dto.setDescription(chartData.getDescription());
        dto.setCategory(chartData.getCategory());
        dto.setIndicator(chartData.getIndicator());
        dto.setChartGrid(chartData.getChartGrid());
        dto.setChartType(chartData.getChartType());

        try {
            dto.setData(objectMapper.readValue(chartData.getData(), new TypeReference<>() {}));
        } catch (Exception e) {
            dto.setData(null);
        }

        return dto;
    }

    // ✅ DTO → 엔티티 변환
    public ChartData toEntity() {
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(this.data);
        } catch (Exception e) {
            jsonData = "[]"; // Default empty array
        }

        return ChartData.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .indicator(this.indicator)
                .chartGrid(this.chartGrid)
                .data(jsonData)
                .chartType(this.chartType)
                .build();
    }
}