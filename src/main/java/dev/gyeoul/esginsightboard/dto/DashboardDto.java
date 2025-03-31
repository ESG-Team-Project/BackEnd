package dev.gyeoul.esginsightboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    // 회사 요약 정보
    private String companyName;
    
    // ESG 점수 요약
    private Double environmentalScore;
    private Double socialScore;
    private Double governanceScore;
    private Double totalScore;
    
    // 카테고리별 데이터 건수
    private Long environmentalItemCount;
    private Long socialItemCount;
    private Long governanceItemCount;
    private Long totalItemCount;
    
    // 검증 상태별 데이터 건수
    private Long verifiedItemCount;
    private Long inVerificationItemCount;
    private Long notVerifiedItemCount;
    
    // 주요 GRI 지표 요약 - 카테고리별 주요 지표 (코드:값 형태) 
    private Map<String, Object> keyEnvironmentalIndicators;
    private Map<String, Object> keySocialIndicators;
    private Map<String, Object> keyGovernanceIndicators;
    
    // 최근 업데이트된 데이터 목록
    private List<GriDataItemDto> recentlyUpdatedItems;
} 