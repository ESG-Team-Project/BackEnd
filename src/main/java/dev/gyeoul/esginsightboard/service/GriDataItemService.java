package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
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
    
    // GRI 데이터 항목 저장
    @Transactional
    public GriDataItemDto saveGriDataItem(GriDataItemDto griDataItemDto) {
        GriDataItem griDataItem = griDataItemDto.toEntity();
        GriDataItem savedGriDataItem = griDataItemRepository.save(griDataItem);
        return GriDataItemDto.fromEntity(savedGriDataItem);
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