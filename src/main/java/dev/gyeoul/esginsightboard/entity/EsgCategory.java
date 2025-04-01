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
@Table(name = "esg_categories") // ESG 카테고리를 저장하는 테이블`
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 보호
public class EsgCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 증가
    private Long id;

    @Column(nullable = false, unique = true) // 필수 값 + 중복 불가
    @NotNull
    private String code; // ESG 코드 ("E", "S", "G")

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