package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.EsgCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsgCategoryDto {
    private Long id;
    private String code;

    public static EsgCategoryDto fromEntity(EsgCategory entity) {
        return EsgCategoryDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .build();
    }
}