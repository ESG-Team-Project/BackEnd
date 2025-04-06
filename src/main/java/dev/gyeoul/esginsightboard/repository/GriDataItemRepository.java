package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.GriDataItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * GRI 데이터 항목에 대한 데이터베이스 액세스를 제공하는 Repository
 * 
 * GRI(Global Reporting Initiative) 데이터는 기업의 ESG(환경, 사회, 지배구조) 보고서에서 
 * 사용되는 표준 지표들을 의미합니다.
 */
@Repository
public interface GriDataItemRepository extends JpaRepository<GriDataItem, Long> {
    
    /**
     * 지정된 범주(E, S, G)에 속하는 GRI 데이터 항목을 조회
     * 
     * @param category ESG 범주 (E: 환경, S: 사회적 책임, G: 지배구조)
     * @return 범주에 해당하는 데이터 항목 목록
     * 
     * 사용 예시: repository.findByCategory("E") - 환경 관련 데이터만 조회
     */
    List<GriDataItem> findByCategory(String category);
    
    /**
     * 표준 코드로 GRI 데이터 항목을 조회
     * 
     * @param standardCode GRI 표준 코드 (예: GRI 302, GRI 305 등)
     * @return 표준 코드에 해당하는 데이터 항목 목록
     * 
     * 사용 예시: repository.findByStandardCode("GRI 302") - 에너지 관련 데이터 조회
     */
    List<GriDataItem> findByStandardCode(String standardCode);
    
    /**
     * 공시 코드로 GRI 데이터 항목을 조회
     * 
     * @param disclosureCode 공시 코드 (예: 302-1, 305-1 등)
     * @return 공시 코드에 해당하는 데이터 항목 목록
     * 
     * 사용 예시: repository.findByDisclosureCode("302-1") - 조직 내 에너지 소비량 데이터 조회
     */
    List<GriDataItem> findByDisclosureCode(String disclosureCode);

    /**
     * 검증 상태별 GRI 데이터 항목을 조회
     * 
     * @param verificationStatus 검증 상태 (예: "검증완료", "검증중", "미검증" 등)
     * @return 검증 상태에 해당하는 데이터 항목 목록
     * 
     * 사용 예시: repository.findByVerificationStatus("검증완료")
     */
    List<GriDataItem> findByVerificationStatus(String verificationStatus);

    /**
     * 특정 보고 기간과 겹치는 GRI 데이터 항목을 조회
     * 
     * 쿼리 조건: reportingPeriodStart <= endDate AND reportingPeriodEnd >= startDate
     * 이는 두 기간이 서로 겹치는지 확인하는 표준 조건입니다.
     * 
     * @param endDate 조회 기간의 종료일
     * @param startDate 조회 기간의 시작일
     * @return 지정된 기간과 겹치는 보고 기간을 가진 데이터 항목 목록
     * 
     * 사용 예시: repository.findItemsByReportingPeriod(
     *             LocalDate.of(2023, 12, 31), LocalDate.of(2023, 1, 1))
     *          - 2023년 데이터 조회
     */
    @Query("SELECT g FROM GriDataItem g WHERE g.reportingPeriodStart <= :endDate AND g.reportingPeriodEnd >= :startDate")
    List<GriDataItem> findItemsByReportingPeriod(@Param("endDate") LocalDate endDate, @Param("startDate") LocalDate startDate);
    
    /**
     * 이전 메서드명: findByReportingPeriodStartLessThanEqualAndReportingPeriodEndGreaterThanEqual
     * 기존 호출 코드와의 호환성을 위해 유지 (신규 코드에서는 findItemsByReportingPeriod 사용 권장)
     * 
     * @deprecated 대신 {@link #findItemsByReportingPeriod(LocalDate, LocalDate)} 사용
     */
    @Deprecated
    default List<GriDataItem> findByReportingPeriodStartLessThanEqualAndReportingPeriodEndGreaterThanEqual(
            LocalDate endDate, LocalDate startDate) {
        return findItemsByReportingPeriod(endDate, startDate);
    }
} 