package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 정보에 대한 데이터베이스 액세스를 제공하는 Repository
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * 회사명으로 회사를 조회
     * 
     * @param name 회사명
     * @return 회사명에 해당하는 회사 정보
     */
    Optional<Company> findByName(String name);
    
    /**
     * 회사명에 포함된 문자열로 회사를 검색
     * 
     * @param name 검색할 회사명 부분
     * @return 검색 조건에 맞는 회사 목록
     */
    List<Company> findByNameContaining(String name);
    
    /**
     * 사업자등록번호로 회사를 조회
     * 
     * @param businessNumber 사업자등록번호
     * @return 사업자등록번호에 해당하는 회사 정보
     */
    Optional<Company> findByBusinessNumber(String businessNumber);
    
    /**
     * 업종으로 회사 목록을 조회
     * 
     * @param industry 업종명
     * @return 업종에 해당하는 회사 목록
     */
    List<Company> findByIndustry(String industry);
    
    /**
     * 섹터로 회사 목록을 조회
     * 
     * @param sector 섹터명
     * @return 섹터에 해당하는 회사 목록
     */
    List<Company> findBySector(String sector);
    
    /**
     * 사업자등록번호 존재 여부 확인
     * 
     * @param businessNumber 사업자등록번호
     * @return 존재 여부
     */
    boolean existsByBusinessNumber(String businessNumber);
} 