package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Borders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ESG 보고서 생성 서비스
 * <p>
 * 이 서비스는 Apache POI를 활용하여 회사의 ESG 데이터를 기반으로 GRI 프레임워크에 맞는 DOCX 형식의 보고서를 생성합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final CompanyRepository companyRepository;
    private final GriDataItemRepository griDataItemRepository;

    // 일반적인 날짜 형식
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    
    // 보고서 제목과 헤더 형식
    private static final String REPORT_TITLE_FORMAT = "%s ESG 지속가능경영보고서";
    private static final String REPORT_SUBTITLE = "GRI Standards 기반";
    private static final String REPORT_PERIOD_FORMAT = "보고 기간: %s ~ %s";
    
    /**
     * 회사 ID를 기반으로 ESG 보고서를 생성합니다.
     * 
     * @param companyId 회사 ID
     * @return 생성된 DOCX 문서의 바이트 배열
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    @Transactional(readOnly = true)
    public byte[] generateEsgReportByCompanyId(Long companyId) throws IOException {
        // 회사 정보 조회
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사 정보를 찾을 수 없습니다: " + companyId));
        
        // GRI 데이터 항목 조회
        List<GriDataItem> dataItems = company.getGriDataItems();
        
        // 빈 데이터 확인
        if (dataItems.isEmpty()) {
            throw new IllegalStateException("보고서에 포함할 ESG 데이터가 없습니다.");
        }
        
        // 보고 기간 결정 (데이터 항목의 보고 기간을 기준으로)
        LocalDate reportStartDate = determineReportStartDate(dataItems);
        LocalDate reportEndDate = determineReportEndDate(dataItems);
        
        // DOCX 문서 생성
        try (XWPFDocument document = new XWPFDocument()) {
            // 문서 제목 및 메타데이터 설정
            createDocumentTitle(document, company.getName(), reportStartDate, reportEndDate);
            
            // 회사 정보 섹션 추가
            addCompanyInfoSection(document, company);
            
            // ESG 데이터를 카테고리별로 그룹화
            Map<String, List<GriDataItem>> categorizedItems = categorizeDataItems(dataItems);
            
            // 환경(E) 섹션 추가
            if (categorizedItems.containsKey("E")) {
                addCategorySection(document, "환경(Environmental)", categorizedItems.get("E"));
            }
            
            // 사회(S) 섹션 추가
            if (categorizedItems.containsKey("S")) {
                addCategorySection(document, "사회(Social)", categorizedItems.get("S"));
            }
            
            // 지배구조(G) 섹션 추가
            if (categorizedItems.containsKey("G")) {
                addCategorySection(document, "지배구조(Governance)", categorizedItems.get("G"));
            }
            
            // 보고서 마무리 섹션 추가 (면책 조항, 연락처 등)
            addFooterSection(document, company.getName());
            
            // 문서를 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 문서 제목과 표지를 생성합니다.
     */
    private void createDocumentTitle(XWPFDocument document, String companyName, 
                                    LocalDate startDate, LocalDate endDate) {
        // 제목 페이지 생성
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        titleParagraph.setSpacingAfter(500);
        
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(String.format(REPORT_TITLE_FORMAT, companyName));
        titleRun.setBold(true);
        titleRun.setFontSize(24);
        titleRun.addBreak();
        titleRun.addBreak();
        
        // 부제목
        XWPFRun subtitleRun = titleParagraph.createRun();
        subtitleRun.setText(REPORT_SUBTITLE);
        subtitleRun.setFontSize(16);
        subtitleRun.addBreak();
        subtitleRun.addBreak();
        
        // 보고 기간
        XWPFRun periodRun = titleParagraph.createRun();
        String formattedStartDate = startDate.format(DATE_FORMATTER);
        String formattedEndDate = endDate.format(DATE_FORMATTER);
        periodRun.setText(String.format(REPORT_PERIOD_FORMAT, formattedStartDate, formattedEndDate));
        periodRun.setFontSize(12);
        periodRun.addBreak();
        periodRun.addBreak();
        
        // 현재 날짜 (보고서 작성일)
        XWPFRun dateRun = titleParagraph.createRun();
        String today = LocalDate.now().format(DATE_FORMATTER);
        dateRun.setText("작성일: " + today);
        dateRun.setFontSize(12);
        
        // 페이지 나누기
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.createRun().addBreak(BreakType.PAGE);
    }
    
    /**
     * 회사 정보 섹션을 추가합니다.
     */
    private void addCompanyInfoSection(XWPFDocument document, Company company) {
        // 회사 정보 섹션 제목
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setText("1. 회사 개요");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // 회사 정보 테이블 생성
        XWPFTable table = document.createTable(5, 2);
        table.setWidth("100%");
        
        // 테이블 스타일 설정
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        tblPr.getTblW().setType(STTblWidth.PCT);
        tblPr.getTblW().setW(new BigInteger("5000"));
        
        // 회사명
        table.getRow(0).getCell(0).setText("회사명");
        table.getRow(0).getCell(1).setText(company.getName());
        
        // 사업자등록번호
        table.getRow(1).getCell(0).setText("사업자등록번호");
        table.getRow(1).getCell(1).setText(company.getBusinessNumber() != null ? 
                company.getBusinessNumber() : "정보 없음");
        
        // 업종
        table.getRow(2).getCell(0).setText("업종");
        table.getRow(2).getCell(1).setText(company.getIndustry() != null ? 
                company.getIndustry() : "정보 없음");
        
        // 섹터
        table.getRow(3).getCell(0).setText("섹터");
        table.getRow(3).getCell(1).setText(company.getSector() != null ? 
                company.getSector() : "정보 없음");
        
        // 직원 수
        table.getRow(4).getCell(0).setText("직원 수");
        table.getRow(4).getCell(1).setText(company.getEmployeeCount() != null ? 
                company.getEmployeeCount().toString() + "명" : "정보 없음");
        
        // 회사 설명이 있는 경우 추가
        if (company.getDescription() != null && !company.getDescription().isEmpty()) {
            XWPFParagraph descParagraph = document.createParagraph();
            descParagraph.setSpacingBefore(300);
            
            XWPFRun descTitleRun = descParagraph.createRun();
            descTitleRun.setText("회사 소개");
            descTitleRun.setBold(true);
            descTitleRun.addBreak();
            
            XWPFRun descRun = descParagraph.createRun();
            descRun.setText(company.getDescription());
        }
        
        // 페이지 나누기
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.createRun().addBreak(BreakType.PAGE);
    }
    
    /**
     * ESG 카테고리별 섹션을 추가합니다.
     */
    private void addCategorySection(XWPFDocument document, String categoryTitle, List<GriDataItem> items) {
        // 카테고리 섹션 제목
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setText(categoryTitle);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // 카테고리별 데이터 항목을 표준 코드 기준으로 그룹화
        Map<String, List<GriDataItem>> standardCodeGroups = items.stream()
                .collect(Collectors.groupingBy(GriDataItem::getStandardCode));
        
        // 각 표준 코드 그룹에 대한 서브섹션 추가
        int count = 0;
        for (Map.Entry<String, List<GriDataItem>> entry : standardCodeGroups.entrySet()) {
            String standardCode = entry.getKey();
            List<GriDataItem> standardItems = entry.getValue();
            
            // 표준 코드 서브섹션 제목
            XWPFParagraph standardTitle = document.createParagraph();
            standardTitle.setStyle("Heading2");
            
            XWPFRun standardRun = standardTitle.createRun();
            // 첫 번째 항목의 공시 제목을 가져와 표준 이름으로 사용
            String disclosureTitle = standardItems.get(0).getDisclosureTitle();
            standardRun.setText(standardCode + " - " + disclosureTitle);
            standardRun.setBold(true);
            standardRun.setFontSize(14);
            
            // 각 공시 항목에 대한 내용 추가
            for (GriDataItem item : standardItems) {
                // 공시 코드 및 제목
                XWPFParagraph disclosureParagraph = document.createParagraph();
                disclosureParagraph.setStyle("Heading3");
                
                XWPFRun disclosureRun = disclosureParagraph.createRun();
                disclosureRun.setText(item.getDisclosureCode() + ": " + item.getDisclosureTitle());
                disclosureRun.setBold(true);
                disclosureRun.setFontSize(12);
                
                // 공시 값(텍스트) 추가
                if (item.getDisclosureValue() != null && !item.getDisclosureValue().isEmpty()) {
                    XWPFParagraph valueParagraph = document.createParagraph();
                    
                    XWPFRun valueRun = valueParagraph.createRun();
                    valueRun.setText(item.getDisclosureValue());
                }
                
                // 정량적 값과 단위 추가
                if (item.getNumericValue() != null) {
                    XWPFParagraph numericParagraph = document.createParagraph();
                    
                    XWPFRun numericRun = numericParagraph.createRun();
                    String unit = item.getUnit() != null ? item.getUnit() : "";
                    numericRun.setText("값: " + item.getNumericValue() + " " + unit);
                    numericRun.setBold(true);
                }
                
                // 보고 기간 추가
                if (item.getReportingPeriodStart() != null && item.getReportingPeriodEnd() != null) {
                    XWPFParagraph periodParagraph = document.createParagraph();
                    
                    XWPFRun periodRun = periodParagraph.createRun();
                    String startDate = item.getReportingPeriodStart().format(DATE_FORMATTER);
                    String endDate = item.getReportingPeriodEnd().format(DATE_FORMATTER);
                    periodRun.setText("보고 기간: " + startDate + " ~ " + endDate);
                    periodRun.setItalic(true);
                    periodRun.setFontSize(10);
                }
                
                // 검증 상태 추가
                if (item.getVerificationStatus() != null && !item.getVerificationStatus().isEmpty()) {
                    XWPFParagraph verificationParagraph = document.createParagraph();
                    
                    XWPFRun verificationRun = verificationParagraph.createRun();
                    String provider = item.getVerificationProvider() != null ? 
                            " (" + item.getVerificationProvider() + ")" : "";
                    verificationRun.setText("검증 상태: " + item.getVerificationStatus() + provider);
                    verificationRun.setItalic(true);
                    verificationRun.setFontSize(10);
                }
                
                // 항목 간 구분선
                if (standardItems.indexOf(item) < standardItems.size() - 1) {
                    XWPFParagraph separator = document.createParagraph();
                    separator.setBorderBottom(Borders.SINGLE);
                    separator.setSpacingAfter(200);
                }
            }
            
            count++;
            // 마지막 표준 코드가 아니면 페이지 나누기 추가
            if (count < standardCodeGroups.size()) {
                XWPFParagraph pageBreak = document.createParagraph();
                pageBreak.createRun().addBreak(BreakType.PAGE);
            }
        }
        
        // 카테고리 섹션 종료 후 페이지 나누기
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.createRun().addBreak(BreakType.PAGE);
    }
    
    /**
     * 보고서 마무리 섹션을 추가합니다.
     */
    private void addFooterSection(XWPFDocument document, String companyName) {
        // 마무리 섹션 제목
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setStyle("Heading1");
        
        XWPFRun titleRun = sectionTitle.createRun();
        titleRun.setText("면책 조항 및 연락처");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // 면책 조항
        XWPFParagraph disclaimerParagraph = document.createParagraph();
        
        XWPFRun disclaimerRun = disclaimerParagraph.createRun();
        disclaimerRun.setText("본 보고서는 " + companyName + "의 ESG 활동과 성과를 GRI 표준에 따라 작성한 것입니다. " +
                "보고서에 포함된 정보는 작성 시점을 기준으로 하며, 예고 없이 변경될 수 있습니다. " +
                "본 보고서에 포함된 정보의 정확성과 완전성을 보장하기 위해 최선을 다하였으나, " +
                "모든 내용이 검증되었음을 의미하지는 않습니다.");
        
        // 연락처
        XWPFParagraph contactParagraph = document.createParagraph();
        contactParagraph.setSpacingBefore(300);
        
        XWPFRun contactTitleRun = contactParagraph.createRun();
        contactTitleRun.setText("문의처");
        contactTitleRun.setBold(true);
        contactTitleRun.addBreak();
        
        XWPFRun contactRun = contactParagraph.createRun();
        contactRun.setText(companyName + " ESG 지속가능경영팀");
        contactRun.addBreak();
        contactRun.setText("이메일: esg@" + companyName.toLowerCase().replaceAll("\\s+", "") + ".com");
    }
    
    /**
     * 데이터 항목들에서 보고 시작일을 결정합니다.
     */
    private LocalDate determineReportStartDate(List<GriDataItem> items) {
        return items.stream()
                .filter(item -> item.getReportingPeriodStart() != null)
                .map(GriDataItem::getReportingPeriodStart)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now().minusYears(1));
    }
    
    /**
     * 데이터 항목들에서 보고 종료일을 결정합니다.
     */
    private LocalDate determineReportEndDate(List<GriDataItem> items) {
        return items.stream()
                .filter(item -> item.getReportingPeriodEnd() != null)
                .map(GriDataItem::getReportingPeriodEnd)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }
    
    /**
     * 데이터 항목들을 ESG 카테고리별로 분류합니다.
     */
    private Map<String, List<GriDataItem>> categorizeDataItems(List<GriDataItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(GriDataItem::getCategory));
    }
} 