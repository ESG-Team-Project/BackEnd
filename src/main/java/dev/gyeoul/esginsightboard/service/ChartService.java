package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.EsgChartDataDto;
import dev.gyeoul.esginsightboard.entity.*;
import dev.gyeoul.esginsightboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final EsgCategoryRepository categoryRepository;
    private final EsgIndicatorRepository indicatorRepository;
    private final EsgInputValueRepository inputValueRepository;
    private final CompanyRepository companyRepository;

    // 차트 데이터 저장 메서드
    public void saveChartData(EsgChartDataDto dto) {

        // 1. 카테고리 조회
        EsgCategory category = categoryRepository.findByCategory(dto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 코드입니다."));

        // 2. 지표 조회 또는 생성
        EsgIndicator indicator = indicatorRepository.findByIndicatorCode(dto.getIndicatorCode())
                .orElseGet(() -> {
                    EsgIndicator newIndicator = new EsgIndicator(
                            "CODE-" + System.currentTimeMillis(), // 고유 코드 생성
                            dto.getIndicatorCode(),
                            "", // 설명 생략
                            category
                    );
                    return indicatorRepository.save(newIndicator);
                });

        // 3. 회사 임시 조회 (1번 ID 고정)
        Company company = companyRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("회사를 찾을 수 없습니다."));

        // 4. 입력값 Map을 순회하며 저장
        for (Map.Entry<String, String> entry : dto.getIndicatorInputs().entrySet()) {
            String period = entry.getKey();   // 예: "2024"
            String value = entry.getValue();  // 예: "123.45" 또는 "사용량 증가"

            Double numericValue = null;
            try {
                numericValue = Double.parseDouble(value);
            } catch (NumberFormatException ignored) {}

            LocalDate startDate = LocalDate.of(Integer.parseInt(period), 1, 1);
            LocalDate endDate = LocalDate.of(Integer.parseInt(period), 12, 31);

            EsgInputValue inputValue = new EsgInputValue(
                    indicator,
                    company,
                    numericValue,
                    value,
                    dto.getUnit(), // 단위는 실제로는 DTO에 추가해도 좋음
                    startDate,
                    endDate
            );

            inputValueRepository.save(inputValue);
        }
    }
}
