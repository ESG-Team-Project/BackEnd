package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.GriDataSearchCriteria;
import dev.gyeoul.esginsightboard.dto.PageResponse;
import dev.gyeoul.esginsightboard.dto.TimeSeriesDataPointDto;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.entity.TimeSeriesDataPoint;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GriDataItemService {

    private final GriDataItemRepository griDataItemRepository;
    private final CompanyRepository companyRepository;
    
    // 모든 GRI 데이터 항목 조회 (N+1 문제 해결)
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getAllGriDataItems() {
        log.debug("모든 GRI 데이터 항목을 조회합니다.");
        return griDataItemRepository.findAll().stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 페이지네이션을 적용한 모든 GRI 데이터 항목 조회
    @Transactional(readOnly = true)
    public PageResponse<GriDataItemDto> getPaginatedGriDataItems(Pageable pageable) {
        log.debug("페이지별 GRI 데이터 항목을 조회합니다. 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<GriDataItemDto> result = griDataItemRepository.findAll(pageable)
                .map(GriDataItemDto::fromEntity);
        return PageResponse.from(result);
    }
    
    // ID로 GRI 데이터 항목 조회 (시계열 데이터 포함)
    @Transactional(readOnly = true)
    public Optional<GriDataItemDto> getGriDataItemById(Long id) {
        log.debug("ID가 {}인 GRI 데이터 항목을 조회합니다.", id);
        return griDataItemRepository.findWithAllDataById(id)
                .map(GriDataItemDto::fromEntity);
    }
    
    // 카테고리(E,S,G)별 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCategory(String category) {
        log.debug("카테고리 {}에 속하는 GRI 데이터 항목을 조회합니다.", category);
        return griDataItemRepository.findByCategory(category).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 GRI 표준 코드에 대한 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByStandardCode(String standardCode) {
        log.debug("표준 코드 {}에 속하는 GRI 데이터 항목을 조회합니다.", standardCode);
        return griDataItemRepository.findByStandardCode(standardCode).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 공시 코드에 대한 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByDisclosureCode(String disclosureCode) {
        log.debug("공시 코드 {}에 속하는 GRI 데이터 항목을 조회합니다.", disclosureCode);
        return griDataItemRepository.findByDisclosureCode(disclosureCode).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 보고 기간 내의 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByReportingPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("보고 기간 {} ~ {} 내의 GRI 데이터 항목을 조회합니다.", startDate, endDate);
        return griDataItemRepository.findItemsByReportingPeriod(endDate, startDate).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 검증 상태별 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByVerificationStatus(String verificationStatus) {
        log.debug("검증 상태가 {}인 GRI 데이터 항목을 조회합니다.", verificationStatus);
        return griDataItemRepository.findByVerificationStatus(verificationStatus).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 회사의 모든 GRI 데이터 항목 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCompanyId(Long companyId) {
        log.debug("회사 ID {}에 속하는 GRI 데이터 항목을 조회합니다.", companyId);
        return griDataItemRepository.findByCompanyId(companyId).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 검색 조건 기반 GRI 데이터 항목 필터링
    @Transactional(readOnly = true)
    public PageResponse<GriDataItemDto> findByCriteria(GriDataSearchCriteria criteria, Pageable pageable) {
        log.debug("검색 조건 기반으로 GRI 데이터 항목을 필터링합니다. 조건: {}", criteria);
        Page<GriDataItemDto> result = griDataItemRepository.findAll(createSpecification(criteria), pageable)
                .map(GriDataItemDto::fromEntity);
        return PageResponse.from(result);
    }
    
    // 검색 조건에 맞는 Specification 생성
    private Specification<GriDataItem> createSpecification(GriDataSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(criteria.getCategory())) {
                predicates.add(cb.equal(root.get("category"), criteria.getCategory()));
            }
            
            if (StringUtils.hasText(criteria.getStandardCode())) {
                predicates.add(cb.equal(root.get("standardCode"), criteria.getStandardCode()));
            }
            
            if (StringUtils.hasText(criteria.getDisclosureCode())) {
                predicates.add(cb.like(root.get("disclosureCode"), criteria.getDisclosureCode() + "%"));
            }
            
            if (criteria.getReportingPeriodStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("reportingPeriodStart"), criteria.getReportingPeriodStart()));
            }
            
            if (criteria.getReportingPeriodEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("reportingPeriodEnd"), criteria.getReportingPeriodEnd()));
            }
            
            if (StringUtils.hasText(criteria.getVerificationStatus())) {
                predicates.add(cb.equal(root.get("verificationStatus"), criteria.getVerificationStatus()));
            }
            
            if (criteria.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("company").get("id"), criteria.getCompanyId()));
            }
            
            if (StringUtils.hasText(criteria.getKeyword())) {
                String likePattern = "%" + criteria.getKeyword() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("disclosureTitle"), likePattern),
                    cb.like(root.get("description"), likePattern)
                ));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    // GRI 데이터 저장 (회사 연결) - 데이터 검증 추가
    @Transactional
    public GriDataItemDto saveGriDataItem(GriDataItemDto dto, Long companyId) {
        log.debug("GRI 데이터 항목 저장을 시작합니다. 공시 코드: {}, 회사 ID: {}", dto.getDisclosureCode(), companyId);
        
        // 데이터 검증
        validateGriDataItem(dto, companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
        
        // DTO에서 엔티티 변환 (company 필드는 나중에 설정)
        GriDataItem griDataItem = GriDataItem.builder()
                .standardCode(dto.getStandardCode())
                .disclosureCode(dto.getDisclosureCode())
                .disclosureTitle(dto.getDisclosureTitle())
                .disclosureValue(dto.getDisclosureValue())
                .description(dto.getDescription())
                .numericValue(dto.getNumericValue())
                .unit(dto.getUnit())
                .reportingPeriodStart(dto.getReportingPeriodStart())
                .reportingPeriodEnd(dto.getReportingPeriodEnd())
                .verificationStatus(dto.getVerificationStatus())
                .verificationProvider(dto.getVerificationProvider())
                .category(dto.getCategory())
                .company(company) // 회사 연결
                .build();
        
        // 회사와 GRI 데이터 간의 양방향 관계 설정
        company.addGriDataItem(griDataItem);
        
        // 저장
        GriDataItem savedItem = griDataItemRepository.save(griDataItem);
        log.info("GRI 데이터 항목 저장 완료. ID: {}, 공시 코드: {}", savedItem.getId(), savedItem.getDisclosureCode());
        
        return GriDataItemDto.fromEntity(savedItem);
    }
    
    // GRI 데이터 검증
    private void validateGriDataItem(GriDataItemDto dto, Long companyId) {
        // 필수 필드 검증
        if (!StringUtils.hasText(dto.getStandardCode())) {
            throw new IllegalArgumentException("표준 코드는 필수 항목입니다.");
        }
        
        if (!StringUtils.hasText(dto.getDisclosureCode())) {
            throw new IllegalArgumentException("공시 코드는 필수 항목입니다.");
        }
        
        if (!StringUtils.hasText(dto.getDisclosureTitle())) {
            throw new IllegalArgumentException("공시 제목은 필수 항목입니다.");
        }
        
        if (dto.getReportingPeriodStart() == null) {
            throw new IllegalArgumentException("보고 기간 시작일은 필수 항목입니다.");
        }
        
        if (dto.getReportingPeriodEnd() == null) {
            throw new IllegalArgumentException("보고 기간 종료일은 필수 항목입니다.");
        }
        
        // 보고 기간 검증
        if (dto.getReportingPeriodEnd().isBefore(dto.getReportingPeriodStart())) {
            throw new IllegalArgumentException("보고 기간 종료일은 시작일 이후여야 합니다.");
        }
        
        // 중복 검사
        if (dto.getId() == null && companyId != null && // 신규 저장 시에만 중복 검사
            griDataItemRepository.existsByCompanyIdAndDisclosureCodeAndReportingPeriod(
                companyId, dto.getDisclosureCode(), 
                dto.getReportingPeriodStart(), dto.getReportingPeriodEnd())) {
            throw new IllegalArgumentException(
                    "해당 회사에 동일한 보고 기간의 " + dto.getDisclosureCode() + " 데이터가 이미 존재합니다.");
        }
        
        // 카테고리 유효성 검사
        String category = dto.getCategory();
        if (!StringUtils.hasText(category) || 
            !List.of("Environmental", "Social", "Governance", "Economic", "일반").contains(category)) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + category);
        }
    }
    
    // GRI 데이터 일괄 저장 (같은 회사에 속하는 여러 데이터) - 성능 최적화 및 로깅 추가
    @Transactional
    public List<GriDataItemDto> saveAllGriDataItems(List<GriDataItemDto> dtos, Long companyId) {
        log.debug("GRI 데이터 항목 일괄 저장을 시작합니다. 항목 수: {}, 회사 ID: {}", dtos.size(), companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
        
        // 모든 항목 검증
        for (GriDataItemDto dto : dtos) {
            validateGriDataItem(dto, companyId);
        }
        
        List<GriDataItem> items = dtos.stream()
                .map(dto -> {
                    GriDataItem item = GriDataItem.builder()
                            .standardCode(dto.getStandardCode())
                            .disclosureCode(dto.getDisclosureCode())
                            .disclosureTitle(dto.getDisclosureTitle())
                            .disclosureValue(dto.getDisclosureValue())
                            .description(dto.getDescription())
                            .numericValue(dto.getNumericValue())
                            .unit(dto.getUnit())
                            .reportingPeriodStart(dto.getReportingPeriodStart())
                            .reportingPeriodEnd(dto.getReportingPeriodEnd())
                            .verificationStatus(dto.getVerificationStatus())
                            .verificationProvider(dto.getVerificationProvider())
                            .category(dto.getCategory())
                            .company(company)
                            .build();
                    
                    company.addGriDataItem(item);
                    return item;
                })
                .collect(Collectors.toList());
        
        List<GriDataItem> savedItems = griDataItemRepository.saveAll(items);
        log.info("GRI 데이터 항목 일괄 저장 완료. 저장된 항목 수: {}", savedItems.size());
        
        return savedItems.stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // GRI 데이터 항목 수정 - 로깅 추가
    @Transactional
    public Optional<GriDataItemDto> updateGriDataItem(Long id, GriDataItemDto griDataItemDto) {
        log.debug("ID가 {}인 GRI 데이터 항목 수정을 시작합니다.", id);
        
        Optional<GriDataItem> existingItemOpt = griDataItemRepository.findById(id);
        
        if (existingItemOpt.isEmpty()) {
            log.warn("ID가 {}인 GRI 데이터 항목이 존재하지 않습니다.", id);
            return Optional.empty();
        }
        
        GriDataItem existingItem = existingItemOpt.get();
        
        // 데이터 검증
        griDataItemDto.setId(id);
        validateGriDataItem(griDataItemDto, existingItem.getCompany().getId());
        
        // 엔티티 업데이트 - 회사 관계 유지
        GriDataItem updatedItem = griDataItemDto.toEntity();
        updatedItem.setCompany(existingItem.getCompany());
        
        GriDataItem savedItem = griDataItemRepository.save(updatedItem);
        log.info("ID가 {}인 GRI 데이터 항목 수정 완료.", id);
        
        return Optional.of(GriDataItemDto.fromEntity(savedItem));
    }
    
    // GRI 데이터 항목 삭제 - 로깅 추가
    @Transactional
    public void deleteGriDataItem(Long id) {
        log.debug("ID가 {}인 GRI 데이터 항목 삭제를 시작합니다.", id);
        
        if (!griDataItemRepository.existsById(id)) {
            log.warn("ID가 {}인 GRI 데이터 항목이 존재하지 않습니다.", id);
            throw new IllegalArgumentException("ID가 " + id + "인 GRI 데이터 항목이 존재하지 않습니다.");
        }
        
        griDataItemRepository.deleteById(id);
        log.info("ID가 {}인 GRI 데이터 항목 삭제 완료.", id);
    }

    /**
     * 특정 회사의 모든 GRI 데이터 항목을 Map 형태로 조회
     * <p>
     * 각 GRI 공시 코드를 키로 사용하고, 데이터 항목을 값으로 사용하는 Map을 반환합니다.
     * </p>
     *
     * @param companyId 회사 ID
     * @return GRI 공시 코드를 키로 하는 GRI 데이터 항목 Map
     */
    @Transactional(readOnly = true)
    public Map<String, GriDataItemDto> getGriDataMapByCompanyId(Long companyId) {
        log.debug("회사 ID {}의 GRI 데이터 항목을 Map 형태로 조회합니다.", companyId);
        List<GriDataItem> griDataItems = griDataItemRepository.findByCompanyId(companyId);
        Map<String, GriDataItemDto> griDataMap = new HashMap<>();
        
        for (GriDataItem item : griDataItems) {
            griDataMap.put(item.getDisclosureCode(), GriDataItemDto.fromEntity(item));
        }
        
        return griDataMap;
    }
    
    /**
     * 특정 회사의 모든 GRI 데이터 항목을 Map 형태로 업데이트
     * <p>
     * 클라이언트에서 수정한 GRI 데이터 항목들을 일괄 업데이트합니다.
     * </p>
     *
     * @param companyId 회사 ID
     * @param griDataMap 업데이트할 GRI 데이터 항목 Map
     * @return 업데이트된 GRI 데이터 항목 Map
     */
    @Transactional
    public Map<String, GriDataItemDto> updateGriDataForCompany(Long companyId, Map<String, GriDataItemDto> griDataMap) {
        log.debug("회사 ID {}의 GRI 데이터 항목을 Map 형태로 업데이트합니다. 항목 수: {}", companyId, griDataMap.size());
        
        // 회사 존재 확인
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사 ID " + companyId + "를 찾을 수 없습니다."));
        
        // 현재 회사의 GRI 데이터 항목 조회
        List<GriDataItem> existingItems = griDataItemRepository.findByCompanyId(companyId);
        Map<String, GriDataItem> existingItemMap = new HashMap<>();
        for (GriDataItem item : existingItems) {
            existingItemMap.put(item.getDisclosureCode(), item);
        }
        
        // 업데이트할 항목 처리
        Map<String, GriDataItemDto> updatedMap = new HashMap<>();
        for (Map.Entry<String, GriDataItemDto> entry : griDataMap.entrySet()) {
            String disclosureCode = entry.getKey();
            GriDataItemDto dto = entry.getValue();
            
            // 데이터 검증
            validateGriDataItem(dto, companyId);
            
            GriDataItem item;
            if (existingItemMap.containsKey(disclosureCode)) {
                // 기존 항목 업데이트
                item = existingItemMap.get(disclosureCode);
                item.setStandardCode(dto.getStandardCode());
                item.setDisclosureTitle(dto.getDisclosureTitle());
                item.setDisclosureValue(dto.getDisclosureValue());
                item.setDescription(dto.getDescription());
                item.setNumericValue(dto.getNumericValue());
                item.setUnit(dto.getUnit());
                item.setReportingPeriodStart(dto.getReportingPeriodStart());
                item.setReportingPeriodEnd(dto.getReportingPeriodEnd());
                item.setVerificationStatus(dto.getVerificationStatus());
                item.setVerificationProvider(dto.getVerificationProvider());
                item.setCategory(dto.getCategory());
            } else {
                // 새 항목 생성
                item = GriDataItem.builder()
                        .standardCode(dto.getStandardCode())
                        .disclosureCode(disclosureCode)
                        .disclosureTitle(dto.getDisclosureTitle())
                        .disclosureValue(dto.getDisclosureValue())
                        .description(dto.getDescription())
                        .numericValue(dto.getNumericValue())
                        .unit(dto.getUnit())
                        .reportingPeriodStart(dto.getReportingPeriodStart())
                        .reportingPeriodEnd(dto.getReportingPeriodEnd())
                        .verificationStatus(dto.getVerificationStatus())
                        .verificationProvider(dto.getVerificationProvider())
                        .category(dto.getCategory())
                        .company(company)
                        .build();
                company.addGriDataItem(item);
            }
            
            GriDataItem savedItem = griDataItemRepository.save(item);
            updatedMap.put(disclosureCode, GriDataItemDto.fromEntity(savedItem));
        }
        
        log.info("회사 ID {}의 GRI 데이터 항목 업데이트 완료. 업데이트된 항목 수: {}", companyId, updatedMap.size());
        return updatedMap;
    }
    
    /**
     * 여러 ID에 해당하는 GRI 데이터 항목을 효율적으로 조회합니다.
     * N+1 문제를 방지하기 위해 단일 쿼리를 사용합니다.
     * 
     * @param ids GRI 데이터 항목 ID 목록
     * @return GRI 데이터 항목 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByIds(List<Long> ids) {
        log.debug("{} 개의 ID에 해당하는 GRI 데이터 항목을 조회합니다.", ids.size());
        return griDataItemRepository.findAllWithCompanyByIdIn(ids).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
} 