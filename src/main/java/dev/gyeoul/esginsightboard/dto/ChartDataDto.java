package dev.gyeoul.esginsightboard.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gyeoul.esginsightboard.entity.ChartData;
import dev.gyeoul.esginsightboard.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ChartDataDto {
    private Long id;              // 차트의 고유 식별자
    private Long userId;          // 사용자 ID
    private String title;
    private String description;
    private String category;
    private String indicator;
    private Integer chartGrid;
    private List<ChartDataPoint> data;   // 표준화된 데이터 구조로 변경
    private String chartType;
    private Map<String, Object> style;   // 차트 스타일링 정보
    private LocalDateTime createdAt;     // 생성 시간
    private LocalDateTime updatedAt;     // 수정 시간
    private String createdBy;            // 작성자 정보 (이메일)

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 표준화된 차트 데이터 포인트 클래스
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChartDataPoint {
        private String label;     // 레이블 (예: "1분기")
        private Double value;     // 값 (예: 65.0)
        private String unit;      // 단위 (예: "%")
        private String timestamp; // 시간 정보 (ISO-8601 형식: "2023-01-01T00:00:00Z")
    }

    // ✅ 엔티티 → DTO 변환
    public static ChartDataDto fromEntity(ChartData chartData) {
        ChartDataDto dto = new ChartDataDto();
        dto.setId(chartData.getId());                // 차트 ID 설정
        dto.setUserId(chartData.getUser().getId());  // User ID 설정
        dto.setTitle(chartData.getTitle());
        dto.setDescription(chartData.getDescription());
        dto.setCategory(chartData.getCategory());
        dto.setIndicator(chartData.getIndicator());
        dto.setChartGrid(chartData.getChartGrid());
        dto.setChartType(chartData.getChartType());
        dto.setCreatedAt(chartData.getCreatedAt());  // 생성 시간 설정
        dto.setUpdatedAt(chartData.getUpdatedAt());  // 수정 시간 설정
        
        // 작성자 정보 설정 (사용자 이메일)
        if (chartData.getUser() != null && chartData.getUser().getEmail() != null) {
            dto.setCreatedBy(chartData.getUser().getEmail());
        }

        try {
            // 데이터 구조 변환 (기존 구조를 새로운 구조로 변환)
            String dataJson = chartData.getData();
            List<Map<String, Object>> rawData = objectMapper.readValue(dataJson, new TypeReference<>() {});
            
            // style 필드가 정의되어 있는지 확인
            if (chartData.getStyle() != null && !chartData.getStyle().isEmpty()) {
                dto.setStyle(objectMapper.readValue(chartData.getStyle(), new TypeReference<>() {}));
            } else {
                // 기본 스타일 설정
                Map<String, Object> defaultStyle = new HashMap<>();
                defaultStyle.put("backgroundColor", "#3498db");
                defaultStyle.put("borderColor", "#2980b9");
                defaultStyle.put("tension", 0.1);
                dto.setStyle(defaultStyle);
            }
            
            dto.setData(convertToStandardDataFormat(rawData));
        } catch (Exception e) {
            dto.setData(null);
            dto.setStyle(new HashMap<>());
        }

        return dto;
    }

    // 기존 데이터 형식을 표준화된 형식으로 변환
    private static List<ChartDataPoint> convertToStandardDataFormat(List<Map<String, Object>> rawData) {
        return rawData.stream().map(item -> {
            ChartDataPoint point = new ChartDataPoint();
            
            // 레이블, 값, 단위 설정
            if (item.containsKey("label")) {
                point.setLabel(item.get("label").toString());
            }
            
            if (item.containsKey("value")) {
                Object value = item.get("value");
                if (value instanceof Number) {
                    point.setValue(((Number) value).doubleValue());
                } else if (value instanceof String) {
                    try {
                        point.setValue(Double.parseDouble((String) value));
                    } catch (NumberFormatException e) {
                        point.setValue(0.0);
                    }
                }
            }
            
            if (item.containsKey("unit")) {
                point.setUnit(item.get("unit").toString());
            } else {
                point.setUnit("%"); // 기본 단위
            }
            
            // 시간 정보 설정 (있으면 사용, 없으면 null)
            if (item.containsKey("timestamp")) {
                point.setTimestamp(item.get("timestamp").toString());
            }
            
            return point;
        }).toList();
    }

    // ✅ DTO → 엔티티 변환
    public ChartData toEntity(User user) {
        String jsonData;
        String jsonStyle;
        
        try {
            // 데이터 직렬화
            jsonData = objectMapper.writeValueAsString(this.data);
            // 스타일 직렬화
            jsonStyle = objectMapper.writeValueAsString(this.style);
        } catch (Exception e) {
            jsonData = "[]"; // 기본 빈 배열
            jsonStyle = "{}"; // 기본 빈 객체
        }

        return ChartData.builder()
                .user(user)
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .indicator(this.indicator)
                .chartGrid(this.chartGrid)
                .data(jsonData)
                .style(jsonStyle)
                .chartType(this.chartType)
                .build();
    }
}