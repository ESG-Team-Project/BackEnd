package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.AuditLogDto;
import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.GriDataSearchCriteria;
import dev.gyeoul.esginsightboard.dto.PageResponse;
import dev.gyeoul.esginsightboard.dto.TimeSeriesDataPointDto;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.entity.TimeSeriesDataPoint;
import dev.gyeoul.esginsightboard.mapper.GriDataItemMapper;
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
import jakarta.servlet.http.HttpServletRequest;

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
                    try {
                        dto.setNumericValue(Double.parseDouble(latestPoint.getValue()));
                    } catch (NumberFormatException e) {
                        log.warn("시계열 데이터 값을 Double로 변환할 수 없습니다: {}", latestPoint.getValue());
                        dto.setNumericValue(null);
                    }
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
                        try {
                            dto.setNumericValue(Double.parseDouble(latestPoint.getValue()));
                        } catch (NumberFormatException e) {
                            log.warn("시계열 데이터 값을 Double로 변환할 수 없습니다: {}", latestPoint.getValue());
                            dto.setNumericValue(null);
                        }
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
            // 모든 문자열 필드 길이 제한
            String standardCode = StringUtils.truncate(
                dto.getStandardCode() != null ? dto.getStandardCode() : "", 20);
            String disclosureCode = StringUtils.truncate(
                dto.getDisclosureCode() != null ? dto.getDisclosureCode() : "", 20);
            String disclosureValue = StringUtils.truncate(
                dto.getDisclosureValue() != null ? dto.getDisclosureValue() : "", 30);
            String numericValue = dto.getNumericValue() != null ? 
                StringUtils.truncate(dto.getNumericValue().toString(), 15) : "";
            String unit = dto.getUnit() != null ? 
                StringUtils.truncate(dto.getUnit(), 10) : "";
            
            // 1. 현재 인증된 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? StringUtils.truncate(auth.getName(), 50) : "system";
            
            // 2. 클라이언트 IP 주소 가져오기
            String ipAddress = null;
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
                ipAddress = StringUtils.truncate(request.getRemoteAddr(), 50);
            }
            
            // 3. 변경 내용 상세 정보 구성 (최종 길이 150자로 제한)
            String details;
            if ("CREATE".equals(action) || "UPDATE".equals(action)) {
                details = String.format("GRI-%s-%s: '%s' (%s %s)", 
                    standardCode, disclosureCode, disclosureValue, numericValue, unit);
            } else {
                details = String.format("GRI-%s-%s 삭제됨", standardCode, disclosureCode);
            }
            
            // 최종 details 문자열 길이 제한 (150자로 제한)
            details = StringUtils.truncate(details, 150);
            
            // 4. 감사 로그 생성
            AuditLogDto auditLog = AuditLogDto.builder()
                .entityType(StringUtils.truncate("GriDataItem", 50))
                .entityId(dto.getId() != null ? StringUtils.truncate(dto.getId().toString(), 50) : "0")
                .action(StringUtils.truncate(action, 20))
                .details(details)
                .username(username)
                .ipAddress(ipAddress)
                .build();
            
            // 별도의 트랜잭션으로 처리 (메인 트랜잭션과 분리)
            new Thread(() -> {
                try {
                    auditLogService.saveAuditLog(auditLog);
                } catch (Exception e) {
                    log.error("감사 로그 별도 스레드 저장 중 오류: {}", e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            log.error("감사 로그 생성 중 오류 발생: {}", e.getMessage());
            // 감사 로그 생성 실패해도 메인 트랜잭션은 롤백되지 않도록 처리
            // 예외를 재발생시키지 않음
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
     * 
     * @param dto 저장할 GRI 데이터 항목 DTO
     * @return 저장된 GRI 데이터 항목 DTO
     */
    public GriDataItemDto saveGriDataItem(GriDataItemDto dto) {
        log.debug("GRI 데이터 항목을 저장합니다: {}-{}", dto.getStandardCode(), dto.getDisclosureCode());
        
        // 필수 필드 기본값 설정
        ensureRequiredFields(dto);
        
        // 시계열 데이터 처리 
        processTimeSeriesData(dto);
        
        // 엔티티 변환 및 저장
        GriDataItem entity = griDataItemMapper.toEntity(dto);
        
        // 회사 설정 (회사 ID가 있는 경우)
        if (dto.getCompanyId() != null) {
            companyRepository.findById(dto.getCompanyId()).ifPresent(entity::setCompany);
        }
        
        // 저장
        GriDataItem savedEntity = griDataItemRepository.save(entity);
        
        // DTO로 변환
        GriDataItemDto savedDto = processTimeSeriesDataForRead(griDataItemMapper.toDto(savedEntity));
        
        // 감사 로그 생성 (별도 트랜잭션에서 처리, 실패해도 메인 트랜잭션에 영향 없음)
        try {
            createAuditLog(dto, dto.getId() == null ? "CREATE" : "UPDATE");
        } catch (Exception e) {
            log.error("감사 로그 생성 중 오류 발생 (무시됨): {}", e.getMessage());
            // 로그 생성 실패해도 메인 작업은 계속 진행
        }
        
        return savedDto;
    }

    /**
     * DTO의 필수 필드가 모두 설정되었는지 확인하고, 누락된 경우 기본값 설정
     * 
     * @param dto 검사할 GriDataItemDto
     */
    private void ensureRequiredFields(GriDataItemDto dto) {
        // 카테고리 필드 기본값 설정
        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            log.debug("카테고리 필드가 누락되어 기본값을 설정합니다. standardCode={}, disclosureCode={}", 
                     dto.getStandardCode(), dto.getDisclosureCode());
            
            // 표준 코드를 기반으로 카테고리 결정 시도
            String standardCode = dto.getStandardCode();
            if (standardCode != null && !standardCode.trim().isEmpty()) {
                try {
                    int codeNumber = Integer.parseInt(standardCode.replaceAll("[^0-9]", ""));
                    if (codeNumber >= 200 && codeNumber < 300) {
                        dto.setCategory(GriDataItemDto.CATEGORY_GOVERNANCE);
                    } else if (codeNumber >= 300 && codeNumber < 400) {
                        dto.setCategory(GriDataItemDto.CATEGORY_ENVIRONMENTAL);
                    } else if (codeNumber >= 400 && codeNumber < 500) {
                        dto.setCategory(GriDataItemDto.CATEGORY_SOCIAL);
                    } else {
                        dto.setCategory("기타");
                    }
                } catch (NumberFormatException e) {
                    // 코드 파싱 실패 시 기본값 설정
                    dto.setCategory("기타");
                }
            } else {
                dto.setCategory("기타");
            }
        }
        
        // disclosure_title 필드 기본값 설정
        if (dto.getDisclosureTitle() == null || dto.getDisclosureTitle().trim().isEmpty()) {
            log.debug("disclosure_title 필드가 누락되어 기본값을 설정합니다.");
            
            // disclosureCode 기반으로 타이틀 생성 시도
            String disclosureCode = dto.getDisclosureCode();
            if (disclosureCode != null && !disclosureCode.trim().isEmpty()) {
                dto.setDisclosureTitle("GRI " + disclosureCode + " 항목");
            } else {
                dto.setDisclosureTitle("미지정 GRI 항목");
            }
        }
        
        // 기타 필수 필드 기본값 설정
        if (dto.getDataType() == null || dto.getDataType().trim().isEmpty()) {
            dto.setDataType(GriDataItem.DataType.TEXT.name());
        }
    }
    
    /**
     * 회사 ID를 지정하여 GRI 데이터 항목 저장
     * 
     * @param dto 저장할 GRI 데이터 항목 DTO
     * @param companyId 회사 ID
     * @return 저장된 GRI 데이터 항목 DTO
     */
    public GriDataItemDto saveGriDataItem(GriDataItemDto dto, Long companyId) {
        log.debug("회사 ID {}에 GRI 데이터 항목을 저장합니다: {}-{}", 
                 companyId, dto.getStandardCode(), dto.getDisclosureCode());
        
        // 회사 ID 설정
        dto.setCompanyId(companyId);
        
        // 기본 저장 메서드 호출
        return saveGriDataItem(dto);
    }

    /**
     * 여러 GRI 데이터 항목 저장
     */
    public List<GriDataItemDto> saveGriDataItems(List<GriDataItemDto> dtos) {
        log.debug("{}개의 GRI 데이터 항목을 저장합니다.", dtos.size());
        
        List<GriDataItemDto> result = new ArrayList<>();
        for (GriDataItemDto dto : dtos) {
            result.add(saveGriDataItem(dto));
        }
        
        return result;
    }

    /**
     * GRI 데이터 항목 업데이트
     */
    public GriDataItemDto updateGriDataItem(Long id, GriDataItemDto dto) {
        log.debug("ID가 {}인 GRI 데이터 항목을 업데이트합니다.", id);
        
        // 기존 데이터 조회
        GriDataItem existingEntity = griDataItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID가 " + id + "인 GRI 데이터 항목을 찾을 수 없습니다."));
        
        // ID 설정
        dto.setId(id);
        
        // 시계열 데이터 처리
        processTimeSeriesData(dto);
        
        // 엔티티 업데이트
        griDataItemMapper.updateEntityFromDto(dto, existingEntity);
        
        // 회사 설정 (회사 ID가 있는 경우)
        if (dto.getCompanyId() != null) {
            companyRepository.findById(dto.getCompanyId()).ifPresent(existingEntity::setCompany);
        }
        
        // 저장
        GriDataItem updatedEntity = griDataItemRepository.save(existingEntity);
        
        // 감사 로그 생성
        createAuditLog(dto, "UPDATE");
        
        // DTO로 변환하여 반환
        return processTimeSeriesDataForRead(griDataItemMapper.toDto(updatedEntity));
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
        log.info("회사 ID {}의 GRI 데이터 맵 조회 시작", companyId);
        
        List<GriDataItem> items = griDataItemRepository.findAllByCompanyId(companyId);
        log.info("회사 ID {}의 GRI 데이터 {}개 항목 조회됨", companyId, items.size());
        
        Map<String, GriDataItemDto> result = new HashMap<>();
        
        for (GriDataItem item : items) {
            String key = item.getStandardCode() + "-" + item.getDisclosureCode();
            GriDataItemDto dto = griDataItemMapper.toDto(item);
            dto = processTimeSeriesDataForRead(dto);
            result.put(key, dto);
            log.debug("GRI 항목 맵에 추가: {}-{}, ID={}, 값={}", 
                     item.getStandardCode(), item.getDisclosureCode(), 
                     item.getId(), StringUtils.truncate(item.getDisclosureValue(), 30));
        }
        
        // 데이터 샘플 로깅 (첫 번째 항목)
        if (!items.isEmpty()) {
            GriDataItem sample = items.get(0);
            log.info("GRI 데이터 샘플 - ID: {}, 코드: {}-{}, 최종 수정일: {}", 
                    sample.getId(), sample.getStandardCode(), sample.getDisclosureCode(), 
                    sample.getUpdatedAt());
        }
        
        log.info("회사 ID {}의 GRI 데이터 맵 조회 완료. {}개 항목 반환", companyId, result.size());
        return result;
    }
    
    /**
     * 회사별 GRI 데이터 일괄 업데이트
     *
     * @param companyId 회사 ID
     * @param griDataMap GRI 데이터 맵 (key: 공시코드, value: GRI 데이터 DTO)
     * @return 업데이트된 GRI 데이터 맵
     */
    @Transactional
    public Map<String, GriDataItemDto> updateGriDataForCompany(
            Long companyId, Map<String, GriDataItemDto> griDataMap) {
        
        log.info("회사 ID {}의 GRI 데이터 맵을 업데이트합니다. 항목 수: {}", companyId, griDataMap.size());
        
        // 기존 데이터 조회 - 실제 엔티티 맵으로 가져오기
        Map<String, GriDataItem> existingEntities = new HashMap<>();
        List<GriDataItem> existingItems = griDataItemRepository.findAllByCompanyId(companyId);
        
        // 키 기반 조회용 맵 구성
        for (GriDataItem item : existingItems) {
            String key = item.getStandardCode() + "-" + item.getDisclosureCode();
            existingEntities.put(key, item);
            log.debug("기존 GRI 항목 로드: {}-{}, ID={}", item.getStandardCode(), item.getDisclosureCode(), item.getId());
        }
        
        log.info("기존 GRI 항목 {}개 로드됨", existingEntities.size());
        
        // 각 항목 처리 및 감사 로그 생성
        Map<String, GriDataItemDto> result = new HashMap<>();
        List<GriDataItem> updatedEntities = new ArrayList<>();
        
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
                
                // 필수 필드 설정
                ensureRequiredFields(newData);
                
                GriDataItem entity;
                boolean isNew = false;
                
                // 기존 엔티티 찾기
                if (existingEntities.containsKey(key)) {
                    // 기존 엔티티 업데이트
                    entity = existingEntities.get(key);
                    log.debug("기존 엔티티 업데이트: {}-{}, ID={}", parts[0], parts[1], entity.getId());
                    // 명시적으로 엔티티 업데이트
                    updateEntityFromDto(newData, entity);
                } else {
                    // 새 엔티티 생성
                    entity = griDataItemMapper.toEntity(newData);
                    
                    // 회사 참조 설정
                    Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
                    entity.setCompany(company);
                    isNew = true;
                    log.debug("새 엔티티 생성: {}-{}", parts[0], parts[1]);
                }
                
                // 명시적 저장 (saveAndFlush로 즉시 DB 반영)
                GriDataItem savedEntity = griDataItemRepository.saveAndFlush(entity);
                updatedEntities.add(savedEntity);
                
                // 감사 로그 생성
                try {
                    createAuditLog(griDataItemMapper.toDto(savedEntity), isNew ? "CREATE" : "UPDATE");
                } catch (Exception e) {
                    log.warn("감사 로그 생성 중 오류 발생 (무시됨): {}", e.getMessage());
                }
                
                // 결과 맵에 추가
                GriDataItemDto savedDto = griDataItemMapper.toDto(savedEntity);
                result.put(key, savedDto);
                
                log.info("GRI 데이터 {}됨: {}-{}, ID={}, 값='{}'", isNew ? "생성" : "업데이트", 
                        savedEntity.getStandardCode(), savedEntity.getDisclosureCode(), 
                        savedEntity.getId(), StringUtils.truncate(savedEntity.getDisclosureValue(), 30));
            }
        }
        
        // 명시적 전체 저장 및 flush 추가
        if (!updatedEntities.isEmpty()) {
            griDataItemRepository.saveAllAndFlush(updatedEntities);
        }
        entityManager.flush();
        log.info("회사 ID {}의 GRI 데이터 업데이트 완료. 처리된 항목 수: {}", companyId, result.size());
        
        // 저장 후 DB 상태 검증 (동일 트랜잭션 내에서)
        verifyDataPersistence(companyId, updatedEntities);
        
        return result;
    }
    
    /**
     * 데이터가 정상적으로 저장되었는지 검증하는 메소드
     * 
     * @param companyId 회사 ID
     * @param updatedEntities 업데이트된 엔티티 목록
     */
    private void verifyDataPersistence(Long companyId, List<GriDataItem> updatedEntities) {
        log.info("회사 ID {}의 데이터 저장 상태 검증 시작", companyId);
        
        // EntityManager를 통한 직접 쿼리 실행 (캐시 무시)
        entityManager.flush(); // 변경사항 강제 반영
        entityManager.clear(); // 영속성 컨텍스트 캐시 비우기
        
        // 검증용 쿼리 실행
        List<GriDataItem> verifiedItems = griDataItemRepository.findAllByCompanyId(companyId);
        
        log.info("검증: DB에서 {}개 항목 확인됨", verifiedItems.size());
        
        // 수정된 항목 중 첫 번째 항목으로 샘플 검증
        if (!updatedEntities.isEmpty() && !verifiedItems.isEmpty()) {
            GriDataItem updatedSample = updatedEntities.get(0);
            
            // ID로 매칭되는 항목 찾기
            Optional<GriDataItem> verifiedItemOpt = verifiedItems.stream()
                    .filter(item -> item.getId().equals(updatedSample.getId()))
                    .findFirst();
            
            if (verifiedItemOpt.isPresent()) {
                GriDataItem verifiedItem = verifiedItemOpt.get();
                boolean valuesMatch = verifiedItem.getDisclosureValue().equals(updatedSample.getDisclosureValue());
                
                log.info("검증 샘플 - ID: {}, 값 일치: {}, DB 값: '{}', 메모리 값: '{}'", 
                        updatedSample.getId(), 
                        valuesMatch,
                        StringUtils.truncate(verifiedItem.getDisclosureValue(), 30),
                        StringUtils.truncate(updatedSample.getDisclosureValue(), 30));
                
                if (!valuesMatch) {
                    log.warn("검증 실패: 저장 후 데이터 불일치 감지. 트랜잭션 관리 확인 필요");
                }
            } else {
                log.warn("검증 실패: ID가 {}인 항목을 저장 후 DB에서 찾을 수 없음", updatedSample.getId());
            }
        }
    }
    
    /**
     * DTO에서 엔티티로 필드 업데이트
     */
    private void updateEntityFromDto(GriDataItemDto dto, GriDataItem entity) {
        // 시계열 데이터 처리
        processTimeSeriesData(dto);
        
        // 필드 복사
        entity.setStandardCode(dto.getStandardCode());
        entity.setDisclosureCode(dto.getDisclosureCode());
        
        if (dto.getDisclosureTitle() != null) {
            entity.setDisclosureTitle(dto.getDisclosureTitle());
        }
        
        if (dto.getDisclosureValue() != null) {
            entity.setDisclosureValue(dto.getDisclosureValue());
        }
        
        entity.setNumericValue(dto.getNumericValue());
        
        if (dto.getUnit() != null) {
            entity.setUnit(dto.getUnit());
        }
        
        if (dto.getCategory() != null) {
            entity.setCategory(dto.getCategory());
        }
        
        if (dto.getReportingPeriodStart() != null) {
            entity.setReportingPeriodStart(dto.getReportingPeriodStart());
        }
        
        if (dto.getReportingPeriodEnd() != null) {
            entity.setReportingPeriodEnd(dto.getReportingPeriodEnd());
        }
        
        if (dto.getVerificationStatus() != null) {
            entity.setVerificationStatus(dto.getVerificationStatus());
        }
        
        if (dto.getVerificationProvider() != null) {
            entity.setVerificationProvider(dto.getVerificationProvider());
        }
        
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        
        if (dto.getDataType() != null) {
            entity.setDataType(GriDataItem.DataType.valueOf(dto.getDataType()));
        }
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

    /**
     * 특정 회사의 여러 GRI 데이터 항목을 일괄 저장
     *
     * @param companyId 회사 ID
     * @param griDataItems 저장할 GRI 데이터 항목 목록
     * @return 저장된 GRI 데이터 항목 목록
     */
    @Transactional
    public List<GriDataItemDto> batchSaveGriData(Long companyId, List<GriDataItemDto> griDataItems) {
        log.debug("회사 ID {}의 GRI 데이터 일괄 저장 요청. 항목 수: {}", companyId, griDataItems.size());
        
        // 각 항목에 회사 ID 설정 (누락된 경우)
        griDataItems.forEach(item -> {
            if (item.getCompanyId() == null) {
                item.setCompanyId(companyId);
            }
            // 필수 필드 설정
            ensureRequiredFields(item);
        });
        
        // 데이터 유효성 검사
        List<GriDataItemDto> validItems = griDataItems.stream()
            .filter(item -> item.getStandardCode() != null && !item.getStandardCode().isEmpty() && 
                          item.getDisclosureCode() != null && !item.getDisclosureCode().isEmpty())
            .collect(Collectors.toList());
        
        if (validItems.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 데이터 저장
        List<GriDataItemDto> savedItems = new ArrayList<>();
        for (GriDataItemDto item : validItems) {
            try {
                GriDataItemDto savedItem = saveGriDataItem(item);
                savedItems.add(savedItem);
            } catch (Exception e) {
                log.error("GRI 데이터 저장 오류: {}", e.getMessage());
            }
        }
        
        return savedItems;
    }

    /**
     * 특정 회사의 특정 GRI 카테고리 데이터를 저장
     *
     * @param companyId 회사 ID
     * @param categoryId 카테고리 ID (예: GRI-302-1)
     * @param categoryData 저장할 GRI 데이터
     * @return 저장된 GRI 데이터 항목
     */
    @Transactional
    public GriDataItemDto saveSingleCategory(Long companyId, String categoryId, GriDataItemDto categoryData) {
        log.debug("회사 ID {}의 카테고리 {} 데이터 저장 요청", companyId, categoryId);
        
        // 카테고리 ID 형식 검증 및 파싱
        String[] parts = categoryId.split("-");
        if (parts.length < 2) {
            log.warn("잘못된 카테고리 ID 형식: {}", categoryId);
            throw new IllegalArgumentException("잘못된 카테고리 ID 형식: " + categoryId);
        }
        
        // 카테고리 데이터 설정
        categoryData.setCompanyId(companyId);
        categoryData.setStandardCode(parts[0]);
        categoryData.setDisclosureCode(parts[1]);
        
        // 기존 데이터 조회 (업데이트인 경우)
        Optional<GriDataItemDto> existingData = findByCompanyIdAndStandardCodeAndDisclosureCode(
            companyId, parts[0], parts[1]);
        
        if (existingData.isPresent()) {
            categoryData.setId(existingData.get().getId());
        }
        
        // 데이터 저장
        try {
            return saveGriDataItem(categoryData);
        } catch (Exception e) {
            log.error("카테고리 {} 데이터 저장 오류: {}", categoryId, e.getMessage());
            throw new RuntimeException("카테고리 데이터 저장 중 오류 발생", e);
        }
    }
} 