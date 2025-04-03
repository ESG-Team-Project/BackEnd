package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.EsgChartDataDto;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.entity.*;
import dev.gyeoul.esginsightboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

// 사용자가 입력한 차트 로직을 처리하는 서비스
@Service
@RequiredArgsConstructor    // 생성자 주입을 자동으로 처리해주는 롬복 어노테이션
public class ChartService {

    private final EsgCategoryRepository categoryRepository;
    private final EsgIndicatorRepository indicatorRepository;
    private final EsgInputValueRepository inputValueRepository;
//    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    // 차트 데이터 저장 메서드
    public void saveChartData(EsgChartDataDto chartDto, UserDto user) {

        // 1. 카테고리 조회 (예: "E", "S", "G")
        EsgCategory category = categoryRepository.findByCategory(chartDto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 코드입니다."));

        // 2. 지표 조회
        EsgIndicator indicator = indicatorRepository.findByIndicatorCode(chartDto.getIndicatorCode())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리 코드입니다."));

        // 3. 사용자 조회
        User userId = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("회사를 찾을 수 없습니다."));

        // 4. 연도별 입력값 Map 순회 저장
        for (Map.Entry<String, String> entry : chartDto.getIndicatorInputs().entrySet()) {
            String period = entry.getKey();   // 예: "2024"
            String value = entry.getValue();  // 예: "123.45" 또는 "사용량 증가"

            Double numericValue = null;
            try {
                // 문자열을 숫자로 변환 시도
                numericValue = Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
                // 숫자가 아닌 경우(null 유지), 예: "증가함", "없음" 등
            }

            // 연도를 기준으로 보고 기간 설정 (1월 1일 ~ 12월 31일)
            LocalDate startDate = LocalDate.of(Integer.parseInt(period), 1, 1);
            LocalDate endDate = LocalDate.of(Integer.parseInt(period), 12, 31);

            // EsqInputValue 엔티티 생성
            EsgInputValue inputValue = new EsgInputValue(
                    indicator,          // 연결된 지표
                    userId,            // 연결된 회사
                    numericValue,       // 숫자형 값 (null 가능)
                    value,              // 원래의 문자열 값
                    chartDto.getUnit(),      // 단위 (예: "톤", "kWh")
                    startDate,          // 보고 시작일
                    endDate             // 보고 종료일
            );

            EsgInputValue saved = inputValueRepository.save(inputValue);
//            savedDtos.add(EsgInputValueDto.from(saved));

            // DB에 저장
//            inputValueRepository.save(inputValue);
        }
//        return saveDtos;
    }
}
