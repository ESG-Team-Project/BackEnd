package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.DashboardDto;
import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GriDataItemRepository griDataItemRepository;
    private final GriDataItemService griDataItemService;
    
    private static final String COMPANY_NAME = "ESG인사이트보드";
    
    // 카테고리별 주요 GRI 표준 코드 목록
    private static final List<String> KEY_ENVIRONMENTAL_CODES = List.of("302-1", "303-3", "305-1");
    private static final List<String> KEY_SOCIAL_CODES = List.of("401-1", "403-9", "405-1");
    private static final List<String> KEY_GOVERNANCE_CODES = List.of("205-2", "206-1", "207-1");
    
    /**
     * 대시보드 종합 정보를 가져옵니다.
     * @return 대시보드 정보 DTO
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboardInfo() {
        // 전체 데이터 가져오기
        List<GriDataItem> allItems = griDataItemRepository.findAll();
        
        // 카테고리별 집계
        long environmentalCount = countByCategory(allItems, "Environmental");
        long socialCount = countByCategory(allItems, "Social");
        long governanceCount = countByCategory(allItems, "Governance");
        
        // 검증 상태별 집계
        long verifiedCount = countByVerificationStatus(allItems, "검증완료");
        long inVerificationCount = countByVerificationStatus(allItems, "검증중");
        long notVerifiedCount = countByVerificationStatus(allItems, "미검증");
        
        // 카테고리별 평균 점수 계산 (numericValue 기반)
        Double environmentalScore = calculateCategoryScore(allItems, "Environmental");
        Double socialScore = calculateCategoryScore(allItems, "Social");
        Double governanceScore = calculateCategoryScore(allItems, "Governance");
        
        // 종합 점수 계산 (각 카테고리 점수 평균)
        Double totalScore = calculateTotalScore(environmentalScore, socialScore, governanceScore);
        
        // 주요 지표 데이터 수집
        Map<String, Object> keyEnvironmentalIndicators = getKeyIndicators(allItems, KEY_ENVIRONMENTAL_CODES);
        Map<String, Object> keySocialIndicators = getKeyIndicators(allItems, KEY_SOCIAL_CODES);
        Map<String, Object> keyGovernanceIndicators = getKeyIndicators(allItems, KEY_GOVERNANCE_CODES);
        
        // 최근 업데이트된 항목 5개 가져오기
        List<GriDataItemDto> recentItems = allItems.stream()
                .sorted((i1, i2) -> i2.getUpdatedAt().compareTo(i1.getUpdatedAt()))
                .limit(5)
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
        
        // 대시보드 DTO 조합
        return DashboardDto.builder()
                .companyName(COMPANY_NAME)
                .environmentalScore(environmentalScore)
                .socialScore(socialScore)
                .governanceScore(governanceScore)
                .totalScore(totalScore)
                .environmentalItemCount(environmentalCount)
                .socialItemCount(socialCount)
                .governanceItemCount(governanceCount)
                .totalItemCount((long) allItems.size())
                .verifiedItemCount(verifiedCount)
                .inVerificationItemCount(inVerificationCount)
                .notVerifiedItemCount(notVerifiedCount)
                .keyEnvironmentalIndicators(keyEnvironmentalIndicators)
                .keySocialIndicators(keySocialIndicators)
                .keyGovernanceIndicators(keyGovernanceIndicators)
                .recentlyUpdatedItems(recentItems)
                .build();
    }
    
    /**
     * 카테고리별 항목 수를 집계합니다.
     */
    private long countByCategory(List<GriDataItem> items, String category) {
        return items.stream()
                .filter(item -> category.equals(item.getCategory()))
                .count();
    }
    
    /**
     * 검증 상태별 항목 수를 집계합니다.
     */
    private long countByVerificationStatus(List<GriDataItem> items, String status) {
        return items.stream()
                .filter(item -> status.equals(item.getVerificationStatus()))
                .count();
    }
    
    /**
     * 카테고리별 평균 점수를 계산합니다.
     */
    private Double calculateCategoryScore(List<GriDataItem> items, String category) {
        List<Double> scores = items.stream()
                .filter(item -> category.equals(item.getCategory()))
                .map(GriDataItem::getNumericValue)
                .filter(value -> value != null)
                .collect(Collectors.toList());
        
        return scores.isEmpty() ? 0.0 : scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * 전체 ESG 점수를 계산합니다. (E, S, G 점수의 평균)
     */
    private Double calculateTotalScore(Double environmentalScore, Double socialScore, Double governanceScore) {
        double total = 0.0;
        int count = 0;
        
        if (environmentalScore > 0) {
            total += environmentalScore;
            count++;
        }
        
        if (socialScore > 0) {
            total += socialScore;
            count++;
        }
        
        if (governanceScore > 0) {
            total += governanceScore;
            count++;
        }
        
        return count > 0 ? total / count : 0.0;
    }
    
    /**
     * 주요 지표 값을 수집합니다.
     */
    private Map<String, Object> getKeyIndicators(List<GriDataItem> items, List<String> disclosureCodes) {
        Map<String, Object> indicators = new HashMap<>();
        
        for (String code : disclosureCodes) {
            items.stream()
                    .filter(item -> code.equals(item.getDisclosureCode()))
                    .findFirst()
                    .ifPresent(item -> {
                        if (item.getNumericValue() != null) {
                            indicators.put(code, Map.of(
                                    "value", item.getNumericValue(),
                                    "unit", item.getUnit(),
                                    "title", item.getDisclosureTitle()
                            ));
                        } else {
                            indicators.put(code, Map.of(
                                    "value", item.getDisclosureValue(),
                                    "title", item.getDisclosureTitle()
                            ));
                        }
                    });
        }
        
        return indicators;
    }
    
    /**
     * GriDataItem을 GriDataItemDto로 변환합니다.
     */
    private GriDataItemDto convertToDto(GriDataItem entity) {
        return GriDataItemDto.fromEntity(entity);
    }
} 