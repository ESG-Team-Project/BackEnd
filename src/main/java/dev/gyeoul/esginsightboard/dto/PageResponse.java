package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지네이션 응답을 위한 DTO 클래스
 * <p>
 * 이 클래스는 API에서 페이지네이션 결과를 표준화된 형식으로 반환하기 위해 사용됩니다.
 * 콘텐츠 목록과 함께 페이지 정보와 메타데이터를 포함합니다.
 * </p>
 *
 * @param <T> 페이지에 포함된 아이템의 타입
 */
@Getter
@Setter
@Builder
@Schema(description = "페이지네이션 응답")
public class PageResponse<T> {

    @Schema(description = "현재 페이지 번호 (0부터 시작)")
    private int pageNumber;

    @Schema(description = "페이지 크기")
    private int pageSize;

    @Schema(description = "총 페이지 수")
    private int totalPages;

    @Schema(description = "총 항목 수")
    private long totalElements;

    @Schema(description = "첫 페이지 여부")
    private boolean first;

    @Schema(description = "마지막 페이지 여부")
    private boolean last;

    @Schema(description = "현재 페이지의 항목 수")
    private int numberOfElements;

    @Schema(description = "페이지 콘텐츠")
    private List<T> content;

    /**
     * Spring Data의 Page 객체로부터 PageResponse 객체를 생성합니다.
     *
     * @param page Spring Data의 Page 객체
     * @param <T> 페이지에 포함된 아이템의 타입
     * @return 생성된 PageResponse 객체
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .content(page.getContent())
                .build();
    }
} 