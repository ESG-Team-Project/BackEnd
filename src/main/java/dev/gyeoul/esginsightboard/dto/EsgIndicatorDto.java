package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.EsgIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsgIndicatorDto {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Long categoryId;

    public static EsgIndicatorDto fromEntity(EsgIndicator entity) {
        return EsgIndicatorDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .categoryId(entity.getCategory().getId())
                .build();
    }
}