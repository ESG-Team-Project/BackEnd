package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.ChartDataDto;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.entity.ChartData;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.repository.ChartDataRepository;
import dev.gyeoul.esginsightboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartDataService {

    private final ChartDataRepository chartDataRepository;
    private final UserRepository userRepository;

//    @Transactional
//    public User findUser()

    // ✅ 차트 데이터 저장 (DTO 적용)
    @Transactional
    public ChartDataDto saveChartData(ChartDataDto dto, UserDto userDto) {
        // ✅ UserRepository를 이용하여 User 조회
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userDto.getId()));

        // ✅ User 객체를 포함하여 Entity 변환
        ChartData savedChartData = chartDataRepository.save(dto.toEntity(user));

        return ChartDataDto.fromEntity(savedChartData);
    }

    // ✅ 여러 개의 ID로 차트 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartsByIds(List<Long> ids) {
        return chartDataRepository.findAllById(ids)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ ID로 특정 차트 조회
    @Transactional(readOnly = true)
    public ChartDataDto getChartDataById(Long id) {
        ChartData chartData = chartDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChartData not found with ID: " + id));
        return ChartDataDto.fromEntity(chartData);
    }

    // ✅ 전체 차트 데이터 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getAllChartData() {
        return chartDataRepository.findAll()
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 특정 제목의 차트 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartDataByTitle(String title) {
        return chartDataRepository.findByTitle(title)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 설명에 특정 키워드 포함된 차트 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartDataByDescription(String keyword) {
        return chartDataRepository.findByDescriptionContaining(keyword)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ ESG 카테고리별 차트 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartDataByCategory(String category) {
        return chartDataRepository.findByCategory(category)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 특정 세부 지표(indicator)별 차트 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartDataByIndicator(String indicator) {
        return chartDataRepository.findByIndicator(indicator)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 특정 차트 칸 개수로 조회
    @Transactional(readOnly = true)
    public List<ChartDataDto> getChartDataByChartGrid(Integer chartGrid) {
        return chartDataRepository.findByChartGrid(chartGrid)
                .stream()
                .map(ChartDataDto::fromEntity)
                .collect(Collectors.toList());
    }
    @Transactional
    public ChartDataDto updateChartData(Long id, ChartDataDto dto, UserDto userDto) {
        ChartData chartData = chartDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChartData not found with ID: " + id));

        // ✅ 엔티티 내부에서 값 변경 (setter 없이)
        chartData.update(
                dto.getTitle(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getIndicator(),
                dto.getChartType(),
                dto.getChartGrid(),
                dto.getDataSets().toString()
        );

        // ✅ 트랜잭션이 끝나면 JPA가 자동으로 변경 사항을 반영 (save() 필요 없음)
        return ChartDataDto.fromEntity(chartData);
    }

    // ✅ 차트 데이터 삭제
    @Transactional
    public void deleteChartData(Long id) {
        if (!chartDataRepository.existsById(id)) {
            throw new IllegalArgumentException("ChartData not found with ID: " + id);
        }
        chartDataRepository.deleteById(id);
    }
}