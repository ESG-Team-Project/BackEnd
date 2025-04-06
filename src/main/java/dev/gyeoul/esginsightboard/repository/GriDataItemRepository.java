package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.GriDataItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * GRI 데이터 항목에 대한 데이터베이스 액세스를 제공하는 Repository
 * 
 * GRI(Global Reporting Initiative) 데이터는 기업의 ESG(환경, 사회, 지배구조) 보고서에서 
 * 사용되는 표준 지표들을 의미합니다.
 */
@Repository
public interface GriDataItemRepository extends JpaRepository<GriDataItem, Long>, 
        JpaSpecificationExecutor<GriDataItem> {
    
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
     * 특정 회사의 GRI 데이터 항목을 조회
     *
     * @param companyId 회사 ID
     * @return 해당 회사의 GRI 데이터 항목 목록
     */
    List<GriDataItem> findByCompanyId(Long companyId);
    
    /**
     * 특정 회사의 특정 카테고리 GRI 데이터 항목을 조회
     *
     * @param companyId 회사 ID
     * @param category 카테고리 (E, S, G)
     * @return 해당 회사의 해당 카테고리 GRI 데이터 항목 목록
     */
    List<GriDataItem> findByCompanyIdAndCategory(Long companyId, String category);

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
    
    /**
     * 회사 엔티티를 포함하여 모든 GRI 데이터 항목을 조회합니다.
     * N+1 쿼리 문제를 방지하기 위해 엔티티 그래프를 사용합니다.
     * 
     * @return 회사 정보가 포함된 GRI 데이터 항목 목록
     */
    @EntityGraph(attributePaths = {"company"})
    List<GriDataItem> findAll();
    
    /**
     * 페이지 단위로 회사 엔티티를 포함하여 GRI 데이터 항목을 조회합니다.
     * 
     * @param pageable 페이지 정보
     * @return 페이지네이션 적용된 GRI 데이터 항목 목록
     */
    @EntityGraph(attributePaths = {"company"})
    Page<GriDataItem> findAll(Pageable pageable);
    
    /**
     * ID로 GRI 데이터 항목을 조회하면서 관련된 모든 시계열 데이터 포인트를 함께 로드합니다.
     * 
     * @param id GRI 데이터 항목 ID
     * @return 시계열 데이터 포인트가 포함된 GRI 데이터 항목
     */
    @EntityGraph(attributePaths = {"company", "timeSeriesDataPoints"})
    Optional<GriDataItem> findWithAllDataById(Long id);
    
    /**
     * 여러 ID에 해당하는 GRI 데이터 항목을 조회하면서 회사 정보를 함께 로드합니다.
     * 
     * @param ids GRI 데이터 항목 ID 목록
     * @return 회사 정보가 포함된 GRI 데이터 항목 목록
     */
    @Query("SELECT g FROM GriDataItem g JOIN FETCH g.company WHERE g.id IN :ids")
    List<GriDataItem> findAllWithCompanyByIdIn(@Param("ids") List<Long> ids);
    
    /**
     * 특정 회사와 카테고리에 해당하는 GRI 데이터 항목을 스트림으로 조회합니다.
     * 대량의 데이터를 처리할 때 메모리 사용량을 최적화합니다.
     * 
     * @param companyId 회사 ID
     * @return GRI 데이터 항목 스트림
     */
    @Query("SELECT g FROM GriDataItem g WHERE g.company.id = :companyId")
    Stream<GriDataItem> findByCompanyIdAsStream(@Param("companyId") Long companyId);
    
    /**
     * 회사 ID, 공시 코드, 보고 기간으로 GRI 데이터 항목 존재 여부를 확인합니다.
     * 데이터 중복 방지를 위해 사용됩니다.
     *
     * @param companyId 회사 ID
     * @param disclosureCode 공시 코드
     * @param startDate 보고 기간 시작일
     * @param endDate 보고 기간 종료일
     * @return 존재 여부 (true/false)
     */
    @Query("SELECT COUNT(g) > 0 FROM GriDataItem g WHERE " +
           "g.company.id = :companyId AND " +
           "g.disclosureCode = :disclosureCode AND " +
           "g.reportingPeriodStart = :startDate AND " +
           "g.reportingPeriodEnd = :endDate")
    boolean existsByCompanyIdAndDisclosureCodeAndReportingPeriod(
            @Param("companyId") Long companyId, 
            @Param("disclosureCode") String disclosureCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 회사, 표준 코드, 공시 코드로 GRI 데이터 항목 조회
     */
    Optional<GriDataItem> findByCompanyIdAndStandardCodeAndDisclosureCode(
            Long companyId, String standardCode, String disclosureCode);
} 