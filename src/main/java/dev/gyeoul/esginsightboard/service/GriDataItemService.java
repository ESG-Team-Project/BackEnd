package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.AuditLogDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GriDataItemService {

    private final GriDataItemRepository griDataItemRepository;
    private final CompanyRepository companyRepository;
    private final GriDataItemMapper griDataItemMapper;
    private final AuditLogService auditLogService;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 시계열 데이터 처리
     */
    private void processTimeSeriesData(GriDataItemDto dto) {
        // 1. DTO에 시계열 데이터가 직접 제공된 경우
        if (dto.getTimeSeriesData() != null && !dto.getTimeSeriesData().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                // JSON 문자열로 변환하여 disclosureValue에 저장
                dto.setDisclosureValue(mapper.writeValueAsString(dto.getTimeSeriesData()));
                
                // 최신 값 또는 평균값을 numericValue에 설정
                TimeSeriesDataPointDto latestPoint = dto.getTimeSeriesData().stream()
                    .max(Comparator.comparing(TimeSeriesDataPointDto::getYear))
                    .orElse(null);
                
                if (latestPoint != null) {
                    dto.setNumericValue(latestPoint.getValue());
                    dto.setUnit(latestPoint.getUnit());
                }
            } catch (Exception e) {
                log.error("시계열 데이터 처리 오류: {}", e.getMessage());
            }
        } 
        // 2. disclosureValue에 JSON 형태로 시계열 데이터가 제공된 경우
        else if (dto.getDisclosureValue() != null && 
                 dto.getDisclosureValue().startsWith("[") && 
                 dto.getDisclosureValue().endsWith("]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<TimeSeriesDataPointDto> timeSeriesData = mapper.readValue(
                    dto.getDisclosureValue(),
                    new TypeReference<List<TimeSeriesDataPointDto>>() {}
                );
                dto.setTimeSeriesData(timeSeriesData);
                
                // 최신 값을 numericValue로 설정
                if (!timeSeriesData.isEmpty()) {
                    TimeSeriesDataPointDto latestPoint = timeSeriesData.stream()
                        .max(Comparator.comparing(TimeSeriesDataPointDto::getYear))
                        .orElse(null);
                    
                    if (latestPoint != null) {
                        dto.setNumericValue(latestPoint.getValue());
                        dto.setUnit(latestPoint.getUnit());
                    }
                }
            } catch (Exception e) {
                log.error("시계열 데이터 파싱 오류: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 감사 로그 생성 메소드
     */
    private void createAuditLog(GriDataItemDto dto, String action) {
        try {
            // 1. 현재 인증된 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "system";
            
            // 2. 클라이언트 IP 주소 가져오기
            String ipAddress = null;
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
                ipAddress = request.getRemoteAddr();
            }
            
            // 3. 변경 내용 상세 정보 구성
            String details;
            if ("CREATE".equals(action) || "UPDATE".equals(action)) {
                details = String.format("GRI-%s-%s: '%s' (%s %s)", 
                    dto.getStandardCode(), 
                    dto.getDisclosureCode(),
                    StringUtils.hasText(dto.getDisclosureValue()) ? 
                        StringUtils.truncate(dto.getDisclosureValue(), 100) : "",
                    dto.getNumericValue() != null ? dto.getNumericValue() : "",
                    dto.getUnit() != null ? dto.getUnit() : "");
            } else {
                details = String.format("GRI-%s-%s 삭제됨", 
                    dto.getStandardCode(), 
                    dto.getDisclosureCode());
            }
            
            // 4. 감사 로그 생성
            AuditLogDto auditLog = AuditLogDto.builder()
                .entityType("GriDataItem")
                .entityId(dto.getId() != null ? dto.getId().toString() : "0")
                .action(action)
                .details(details)
                .username(username)
                .ipAddress(ipAddress)
                .build();
            
            auditLogService.saveAuditLog(auditLog);
        } catch (Exception e) {
            log.error("감사 로그 생성 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 모든 GRI 데이터 항목 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getAllGriDataItems() {
        log.debug("모든 GRI 데이터 항목을 조회합니다.");
        List<GriDataItem> griDataItems = griDataItemRepository.findAll();
        return griDataItems.stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }
    
    /**
     * 조회 시 시계열 데이터 처리
     */
    private GriDataItemDto processTimeSeriesDataForRead(GriDataItemDto dto) {
        if (dto.getDisclosureValue() != null && 
            dto.getDisclosureValue().startsWith("[") && 
            dto.getDisclosureValue().endsWith("]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<TimeSeriesDataPointDto> timeSeriesData = mapper.readValue(
                    dto.getDisclosureValue(), 
                    new TypeReference<List<TimeSeriesDataPointDto>>() {}
                );
                dto.setTimeSeriesData(timeSeriesData);
            } catch (Exception e) {
                log.error("시계열 데이터 읽기 오류: {}", e.getMessage());
            }
        }
        return dto;
    }

    /**
     * ID로 GRI 데이터 항목 조회
     */
    @Transactional(readOnly = true)
    public Optional<GriDataItemDto> getGriDataItemById(Long id) {
        log.debug("ID가 {}인 GRI 데이터 항목을 조회합니다.", id);
        return griDataItemRepository.findById(id)
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead);
    }

    /**
     * 회사, 표준 코드, 공시 코드로 GRI 데이터 항목 조회
     */
    @Transactional(readOnly = true)
    public Optional<GriDataItemDto> findByCompanyIdAndStandardCodeAndDisclosureCode(
            Long companyId, String standardCode, String disclosureCode) {
        
        log.debug("회사 ID {}, 표준 코드 {}, 공시 코드 {}인 GRI 데이터 항목을 조회합니다.", 
                 companyId, standardCode, disclosureCode);
        
        return griDataItemRepository
            .findByCompanyIdAndStandardCodeAndDisclosureCode(companyId, standardCode, disclosureCode)
            .map(griDataItemMapper::toDto)
            .map(this::processTimeSeriesDataForRead);
    }

    /**
     * GRI 데이터 항목 저장
     */
    public GriDataItemDto saveGriDataItem(GriDataItemDto griDataItemDto) {
        log.debug("GRI 데이터 항목을 저장합니다. 공시 코드: {}, 회사 ID: {}", 
                 griDataItemDto.getDisclosureCode(), griDataItemDto.getCompanyId());
        
        boolean isUpdate = griDataItemDto.getId() != null;
        
        // 시계열 데이터 처리
        processTimeSeriesData(griDataItemDto);
        
        // 데이터 저장
        GriDataItem entity = griDataItemMapper.toEntity(griDataItemDto);
        GriDataItem savedEntity = griDataItemRepository.save(entity);
        GriDataItemDto savedDto = griDataItemMapper.toDto(savedEntity);
        
        // 시계열 데이터 다시 처리 (저장된 entity에서)
        savedDto = processTimeSeriesDataForRead(savedDto);
        
        // 감사 로그 생성
        createAuditLog(savedDto, isUpdate ? "UPDATE" : "CREATE");
        
        return savedDto;
    }

    /**
     * 여러 GRI 데이터 항목 저장
     */
    public List<GriDataItemDto> saveGriDataItems(List<GriDataItemDto> griDataItemDtos) {
        log.debug("{}개의 GRI 데이터 항목을 저장합니다.", griDataItemDtos.size());
        return griDataItemDtos.stream()
                .map(this::saveGriDataItem)
                .collect(Collectors.toList());
    }

    /**
     * GRI 데이터 항목 업데이트
     */
    public GriDataItemDto updateGriDataItem(Long id, GriDataItemDto griDataItemDto) {
        log.debug("ID가 {}인 GRI 데이터 항목을 업데이트합니다.", id);
        
        // 기존 데이터 확인
        GriDataItem existingItem = griDataItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 GRI 데이터 항목이 없습니다: " + id));
        
        // ID 설정
        griDataItemDto.setId(id);
        
        // 시계열 데이터 처리
        processTimeSeriesData(griDataItemDto);
        
        // 데이터 업데이트
        GriDataItem updatedEntity = griDataItemMapper.toEntity(griDataItemDto);
        GriDataItem savedEntity = griDataItemRepository.save(updatedEntity);
        GriDataItemDto savedDto = griDataItemMapper.toDto(savedEntity);
        
        // 시계열 데이터 다시 처리
        savedDto = processTimeSeriesDataForRead(savedDto);
        
        // 감사 로그 생성
        createAuditLog(savedDto, "UPDATE");
        
        return savedDto;
    }

    /**
     * GRI 데이터 항목 삭제
     */
    public void deleteGriDataItem(Long id) {
        log.debug("ID가 {}인 GRI 데이터 항목을 삭제합니다.", id);
        
        // 삭제 전 데이터 조회 (감사 로그용)
        Optional<GriDataItemDto> dtoOpt = getGriDataItemById(id);
        
        // 데이터 삭제
        griDataItemRepository.deleteById(id);
        
        // 감사 로그 생성
        dtoOpt.ifPresent(dto -> createAuditLog(dto, "DELETE"));
    }

    /**
     * 페이지네이션을 적용한 모든 GRI 데이터 항목 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<GriDataItemDto> getPaginatedGriDataItems(Pageable pageable) {
        log.debug("페이지별 GRI 데이터 항목을 조회합니다. 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<GriDataItemDto> result = griDataItemRepository.findAll(pageable)
                .map(griDataItemMapper::toDto);
        return PageResponse.from(result);
    }

    /**
     * 카테고리(E,S,G)별 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCategory(String category) {
        log.debug("카테고리 {}에 속하는 GRI 데이터 항목을 조회합니다.", category);
        return griDataItemRepository.findByCategory(category).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 특정 GRI 표준 코드에 대한 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByStandardCode(String standardCode) {
        log.debug("표준 코드 {}에 속하는 GRI 데이터 항목을 조회합니다.", standardCode);
        return griDataItemRepository.findByStandardCode(standardCode).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 특정 공시 코드에 대한 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByDisclosureCode(String disclosureCode) {
        log.debug("공시 코드 {}에 속하는 GRI 데이터 항목을 조회합니다.", disclosureCode);
        return griDataItemRepository.findByDisclosureCode(disclosureCode).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 특정 보고 기간 내의 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByReportingPeriod(LocalDate startDate, LocalDate endDate) {
        log.debug("보고 기간 {} ~ {} 내의 GRI 데이터 항목을 조회합니다.", startDate, endDate);
        return griDataItemRepository.findItemsByReportingPeriod(endDate, startDate).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 검증 상태별 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByVerificationStatus(String verificationStatus) {
        log.debug("검증 상태가 {}인 GRI 데이터 항목을 조회합니다.", verificationStatus);
        return griDataItemRepository.findByVerificationStatus(verificationStatus).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회사의 모든 GRI 데이터 항목 조회
     */
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCompanyId(Long companyId) {
        log.debug("회사 ID {}에 속하는 GRI 데이터 항목을 조회합니다.", companyId);
        return griDataItemRepository.findByCompanyId(companyId).stream()
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }

    /**
     * 검색 조건 기반 GRI 데이터 항목 필터링
     */
    @Transactional(readOnly = true)
    public PageResponse<GriDataItemDto> findByCriteria(GriDataSearchCriteria criteria, Pageable pageable) {
        log.debug("검색 조건 기반으로 GRI 데이터 항목을 필터링합니다. 조건: {}", criteria);
        Page<GriDataItemDto> result = griDataItemRepository.findAll(createSpecification(criteria), pageable)
                .map(griDataItemMapper::toDto);
        return PageResponse.from(result);
    }

    /**
     * 검색 조건에 맞는 Specification 생성
     */
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
            griDataMap.put(item.getDisclosureCode(), griDataItemMapper.toDto(item));
        }
        
        return griDataMap;
    }
    
    /**
     * 회사별 GRI 데이터 맵 업데이트 시 감사 로그 추가
     */
    public Map<String, GriDataItemDto> updateGriDataForCompany(
            Long companyId, Map<String, GriDataItemDto> griDataMap) {
        
        log.debug("회사 ID {}의 GRI 데이터 맵을 업데이트합니다. 항목 수: {}", companyId, griDataMap.size());
        
        // 기존 데이터 조회
        Map<String, GriDataItemDto> existingData = getGriDataMapByCompanyId(companyId);
        
        // 각 항목 처리 및 감사 로그 생성
        Map<String, GriDataItemDto> result = new HashMap<>();
        
        for (Map.Entry<String, GriDataItemDto> entry : griDataMap.entrySet()) {
            String key = entry.getKey();
            GriDataItemDto newData = entry.getValue();
            
            // 회사 ID 설정
            newData.setCompanyId(companyId);
            
            // 키 파싱
            String[] parts = key.split("-");
            if (parts.length >= 2) {
                newData.setStandardCode(parts[0]);
                newData.setDisclosureCode(parts[1]);
                
                // 기존 ID 설정 (있는 경우)
                if (existingData.containsKey(key)) {
                    newData.setId(existingData.get(key).getId());
                }
                
                // 데이터 저장
                GriDataItemDto savedDto = saveGriDataItem(newData);
                result.put(key, savedDto);
            }
        }
        
        return result;
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
                .map(griDataItemMapper::toDto)
                .map(this::processTimeSeriesDataForRead)
                .collect(Collectors.toList());
    }
} 