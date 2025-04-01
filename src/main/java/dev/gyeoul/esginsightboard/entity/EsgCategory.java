package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "esg_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsgCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String code; // "E", "S", "G"

    @Builder // 빌더 패턴을 사용하여 객체 생성
    public EsgCategory(String code) {
        this.code = code;
    }
    public String getName() {
        return switch (this.code) {
            case "E" -> "환경";
            case "S" -> "사회";
            case "G" -> "지배구조";
            default -> "알 수 없음";
        };
    }
}
