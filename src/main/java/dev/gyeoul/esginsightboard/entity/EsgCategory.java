package dev.gyeoul.esginsightboard.entity;

import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsgCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String category; // "E", "S", "G"

    @Builder
    public EsgCategory(Long id, String category) {
        Assert.hasText(category, "카테고리 필수");
        this.category = category;
    }
//    public String getName() {
//        return switch (this.category) {
//            case "E" -> "환경";
//            case "S" -> "사회";
//            case "G" -> "지배구조";
//            default -> "알 수 없음";
//        };
//    }
}
