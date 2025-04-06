package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GriDataItemService {

    private final GriDataItemRepository griDataItemRepository;
    private final CompanyRepository companyRepository;
    
    // 모든 GRI 데이터 항목 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getAllGriDataItems() {
        return griDataItemRepository.findAll().stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // ID로 GRI 데이터 항목 조회
    @Transactional(readOnly = true)
    public Optional<GriDataItemDto> getGriDataItemById(Long id) {
        return griDataItemRepository.findById(id)
                .map(GriDataItemDto::fromEntity);
    }
    
    // 카테고리(E,S,G)별 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCategory(String category) {
        return griDataItemRepository.findByCategory(category).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 GRI 표준 코드에 대한 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByStandardCode(String standardCode) {
        return griDataItemRepository.findByStandardCode(standardCode).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 공시 코드에 대한 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByDisclosureCode(String disclosureCode) {
        return griDataItemRepository.findByDisclosureCode(disclosureCode).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 보고 기간 내의 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByReportingPeriod(LocalDate startDate, LocalDate endDate) {
        return griDataItemRepository.findItemsByReportingPeriod(endDate, startDate).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 검증 상태별 데이터 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByVerificationStatus(String verificationStatus) {
        return griDataItemRepository.findByVerificationStatus(verificationStatus).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 특정 회사의 모든 GRI 데이터 항목 조회
    @Transactional(readOnly = true)
    public List<GriDataItemDto> getGriDataItemsByCompanyId(Long companyId) {
        return griDataItemRepository.findByCompanyId(companyId).stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // GRI 데이터 저장 (회사 연결)
    @Transactional
    public GriDataItemDto saveGriDataItem(GriDataItemDto dto, Long companyId) {
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
        
        return GriDataItemDto.fromEntity(savedItem);
    }
    
    // GRI 데이터 일괄 저장 (같은 회사에 속하는 여러 데이터)
    @Transactional
    public List<GriDataItemDto> saveAllGriDataItems(List<GriDataItemDto> dtos, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
        
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
        
        return savedItems.stream()
                .map(GriDataItemDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // GRI 데이터 항목 수정
    @Transactional
    public Optional<GriDataItemDto> updateGriDataItem(Long id, GriDataItemDto griDataItemDto) {
        return griDataItemRepository.findById(id)
                .map(existingItem -> {
                    // 기존 ID 유지
                    griDataItemDto.setId(id);
                    GriDataItem updatedItem = griDataItemDto.toEntity();
                    return GriDataItemDto.fromEntity(griDataItemRepository.save(updatedItem));
                });
    }
    
    // GRI 데이터 항목 삭제
    @Transactional
    public void deleteGriDataItem(Long id) {
        griDataItemRepository.deleteById(id);
    }
} 