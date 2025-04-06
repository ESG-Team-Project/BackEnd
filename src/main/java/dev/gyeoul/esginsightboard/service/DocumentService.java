package dev.gyeoul.esginsightboard.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import dev.gyeoul.esginsightboard.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 문서 다운로드 및 관리 서비스
 * <p>
 * 다양한 형식의 문서 생성 및 다운로드 기능을 제공합니다.
 * 지원 형식: PDF, DOCX
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final ReportGenerationService reportGenerationService;
    
    private static final Map<String, String> FRAMEWORK_TITLES = new HashMap<>();
    
    private static final DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    static {
        FRAMEWORK_TITLES.put("gri", "GRI 프레임워크");
        FRAMEWORK_TITLES.put("sasb", "SASB 프레임워크");
        FRAMEWORK_TITLES.put("tcfd", "TCFD 프레임워크");
    }

    /**
     * ESG 테이블 데이터를 담는 모델 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EsgTableData {
        private String title;
        private String subtitle;
        private String gri;
        private List<String> headers;
        private List<String> years;
        private Map<String, List<String>> rowData;
        private String note;
    }
    
    /**
     * 프레임워크 문서를 지정된 형식으로 다운로드합니다.
     * 
     * @param frameworkId 프레임워크 ID (예: gri, sasb, tcfd)
     * @param format 문서 형식 (pdf 또는 docx)
     * @return 문서 바이트 배열
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public byte[] getFrameworkDocument(String frameworkId, String format) throws IOException {
        // 파일 경로 생성
        String resourcePath = String.format("/static/documents/%s_framework.%s", frameworkId, format);
        
        try {
            // 리소스 로드 시도
            Resource resource = new ClassPathResource(resourcePath);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    return FileCopyUtils.copyToByteArray(inputStream);
                }
            } else {
                // 리소스가 없는 경우 동적으로 생성
                return generateFrameworkDocument(frameworkId, format);
            }
        } catch (IOException e) {
            log.error("문서 파일 로드 중 오류 발생: {}", e.getMessage());
            throw new IOException("문서 파일을 찾을 수 없습니다: " + resourcePath, e);
        } catch (DocumentException e) {
            log.error("PDF 문서 생성 중 오류 발생: {}", e.getMessage());
            throw new IOException("PDF 문서 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 프레임워크 문서를 동적으로 생성합니다.
     * 
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식
     * @return 생성된 문서 바이트 배열
     * @throws IOException 문서 생성 중 오류 발생 시
     * @throws DocumentException PDF 문서 생성 중 오류 발생 시
     */
    private byte[] generateFrameworkDocument(String frameworkId, String format) throws IOException, DocumentException {
        String title = FRAMEWORK_TITLES.getOrDefault(frameworkId, frameworkId.toUpperCase() + " 프레임워크");
        
        // 예시 테이블 데이터 생성
        List<EsgTableData> tableDataList = createSampleEsgTableData();
        
        if ("pdf".equalsIgnoreCase(format)) {
            return generatePdfDocument(title, frameworkId, tableDataList);
        } else if ("docx".equalsIgnoreCase(format)) {
            return generateDocxDocument(title, frameworkId, tableDataList);
        } else {
            throw new IllegalArgumentException("지원하지 않는 문서 형식: " + format);
        }
    }
    
    /**
     * 샘플 ESG 테이블 데이터를 생성합니다.
     * 
     * @return ESG 테이블 데이터 리스트
     */
    private List<EsgTableData> createSampleEsgTableData() {
        List<EsgTableData> tableDataList = new ArrayList<>();
        
        // 에너지 사용량 테이블
        EsgTableData energyUsageTable = EsgTableData.builder()
                .title("에너지 사용량 상세")
                .subtitle("(단위 : TJ, TJ/십억원)")
                .gri("GRI 302-1")
                .headers(Arrays.asList("구분", "2020년", "2021년", "2022년", "2023년"))
                .years(Arrays.asList("2020년", "2021년", "2022년", "2023년"))
                .rowData(new LinkedHashMap<>() {{
                    put("Fossil fuels", Arrays.asList("88,708", "88,011", "86,437", "86,616"));
                    put("Electricity", Arrays.asList("35,469", "38,995", "37,162", "36,737"));
                    put("Steam / Heating / Cooling & other energy", Arrays.asList("1,611", "3,026", "2,518", "1,715"));
                    put("Total renewable energy", Arrays.asList("", "0.108", "0.108", "0.108"));
                    put("Energy Intensity*", Arrays.asList("", "4.73", "2.97", "3.50"));
                }})
                .note("* Intensity= 배출량을 기반으로 산출")
                .build();
        
        // 환경투자 세부내용 테이블
        EsgTableData environmentInvestmentTable = EsgTableData.builder()
                .title("환경투자 세부내용")
                .subtitle("(단위 : 백만원)")
                .gri("GRI 302-3")
                .headers(Arrays.asList("구분", "2020년", "2021년", "2022년", "2023년"))
                .years(Arrays.asList("2020년", "2021년", "2022년", "2023년"))
                .rowData(new LinkedHashMap<>() {{
                    put("대기·악취·HAPs", Arrays.asList("12,665", "13,419", "22,814", "34,893"));
                    put("수질·해양", Arrays.asList("1,106", "1,761", "1,530", "9,966"));
                    put("에너지 저감·기술지원", Arrays.asList("288", "1,545", "4,586", "22,679"));
                    put("토양·유해화학물질·폐기물·기타", Arrays.asList("6,063", "2,224", "4,406", "2,657"));
                    put("계", Arrays.asList("20,122", "18,949", "33,336", "70,195"));
                }})
                .note("* 2023년 주요 투자 내역\n" +
                      "  대기·악취·HAPs: 충전규제에 따른 TMS 신규 설치, Flare stack 완경계, 형염계 설치 등")
                .build();
        
        // 에너지 비용 테이블
        EsgTableData energyCostTable = EsgTableData.builder()
                .title("에너지 비용")
                .subtitle("(단위 : 백만원)")
                .gri("GRI 302-1")
                .headers(Arrays.asList("구분", "2020년", "2021년", "2022년", "2023년"))
                .years(Arrays.asList("2020년", "2021년", "2022년", "2023년"))
                .rowData(new LinkedHashMap<>() {{
                    put("에너지 비용", Arrays.asList("1,170,767", "1,548,909", "1,709,809", "1,859,448"));
                }})
                .build();
        
        // 대기오염물질 배출농도 테이블
        EsgTableData airEmissionTable = EsgTableData.builder()
                .title("온산공장 대기오염물질 배출농도")
                .gri("GRI 305-7")
                .headers(Arrays.asList("구분", "2020년", "2021년", "2022년", "2023년"))
                .years(Arrays.asList("2020년", "2021년", "2022년", "2023년"))
                .rowData(new LinkedHashMap<>() {{
                    put("황산화물(SOx) 배출농도 (ppm)", Arrays.asList("2.0", "0.7", "0.3", "1.1"));
                    put("황산화물(SOx) 법정 기준 (ppm)", Arrays.asList("120", "120", "120", "94"));
                    put("질소산화물(NOx) 배출농도 (ppm)", Arrays.asList("45.0", "43.0", "37.3", "35.9"));
                    put("질소산화물(NOx) 법정 기준 (ppm)", Arrays.asList("130", "130", "130", "80"));
                    put("먼지(Dust) 배출농도 (mg/Sm³)", Arrays.asList("0.0", "1.4", "1.9", "1.0"));
                    put("먼지(Dust) 법정 기준 (mg/Sm³)", Arrays.asList("15", "15", "15", "15"));
                }})
                .note("* 2023년부터 법정기준은 통합환경허가에 따른 Heater, Boiler의 허가배출기준 평균 적용\n" +
                      "* 당사사업 중 가장 점유율이 높은 Heater, Boiler의 배출농도 단순평균 (TMS 및 자가 측정)")
                .build();
        
        tableDataList.add(energyUsageTable);
        tableDataList.add(environmentInvestmentTable);
        tableDataList.add(energyCostTable);
        tableDataList.add(airEmissionTable);
        
        return tableDataList;
    }
    
    /**
     * PDF 형식의 프레임워크 문서를 생성합니다.
     * 
     * @param title 문서 제목
     * @param frameworkId 프레임워크 ID
     * @param tableDataList ESG 테이블 데이터 리스트
     * @return PDF 바이트 배열
     * @throws IOException 문서 생성 중 오류 발생 시
     * @throws DocumentException PDF 문서 생성 중 오류 발생 시
     */
    private byte[] generatePdfDocument(String title, String frameworkId, List<EsgTableData> tableDataList) throws IOException, DocumentException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 제목 추가
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Paragraph titleParagraph = new com.itextpdf.text.Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);
            
            // 현재 날짜 추가
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
            com.itextpdf.text.Paragraph dateParagraph = new com.itextpdf.text.Paragraph("발행일: " + today, dateFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(40);
            document.add(dateParagraph);
            
            // 본문 내용 추가
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            com.itextpdf.text.Paragraph contentParagraph = new com.itextpdf.text.Paragraph(
                    frameworkId.toUpperCase() + " 프레임워크는 기업의 ESG 성과를 측정하고 보고하기 위한 국제 표준입니다. "
                    + "이 문서는 " + frameworkId.toUpperCase() + " 표준을 준수하여 ESG 보고서를 작성하는 방법을 안내합니다.", 
                    contentFont);
            contentParagraph.setSpacingAfter(20);
            document.add(contentParagraph);
            
            // ESG 테이블 추가
            for (EsgTableData tableData : tableDataList) {
                addPdfTable(document, tableData);
                document.add(new com.itextpdf.text.Paragraph(" "));
                document.add(new com.itextpdf.text.Paragraph(" "));
            }
            
            // 안내 문구 추가
            com.itextpdf.text.Paragraph noteParagraph = new com.itextpdf.text.Paragraph(
                    "※ 참고: 이 문서는 템플릿으로, 실제 보고서 작성 시 기업 특성에 맞게 조정이 필요합니다.", 
                    contentFont);
            noteParagraph.setAlignment(Element.ALIGN_CENTER);
            noteParagraph.setSpacingBefore(40);
            document.add(noteParagraph);
            
            document.close();
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * PDF 문서에 ESG 테이블을 추가합니다.
     * 
     * @param document PDF 문서
     * @param tableData ESG 테이블 데이터
     * @throws DocumentException 테이블 추가 시 오류 발생 시
     */
    private void addPdfTable(com.itextpdf.text.Document document, EsgTableData tableData) throws DocumentException {
        // 테이블 제목 추가
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        com.itextpdf.text.Paragraph titleParagraph = new com.itextpdf.text.Paragraph(tableData.getTitle(), titleFont);
        
        if (tableData.getSubtitle() != null && !tableData.getSubtitle().isEmpty()) {
            titleParagraph.add(new Phrase(" " + tableData.getSubtitle(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        }
        
        titleParagraph.setSpacingAfter(5);
        document.add(titleParagraph);
        
        // GRI 정보 추가 (오른쪽 정렬)
        if (tableData.getGri() != null && !tableData.getGri().isEmpty()) {
            Font griFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            com.itextpdf.text.Paragraph griParagraph = new com.itextpdf.text.Paragraph(tableData.getGri(), griFont);
            griParagraph.setAlignment(Element.ALIGN_RIGHT);
            griParagraph.setSpacingAfter(5);
            document.add(griParagraph);
        }
        
        // 테이블 생성
        PdfPTable table = new PdfPTable(tableData.getHeaders().size());
        table.setWidthPercentage(100);
        
        // 헤더 생성
        for (String header : tableData.getHeaders()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            cell.setBackgroundColor(new BaseColor(240, 240, 240));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        // 데이터 행 추가
        for (Map.Entry<String, List<String>> row : tableData.getRowData().entrySet()) {
            // 행 이름 셀
            PdfPCell labelCell = new PdfPCell(new Phrase(row.getKey(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            labelCell.setPadding(5);
            table.addCell(labelCell);
            
            // 데이터 셀
            List<String> rowValues = row.getValue();
            for (int i = 0; i < tableData.getYears().size(); i++) {
                String value = (i < rowValues.size()) ? rowValues.get(i) : "";
                PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
        
        document.add(table);
        
        // 테이블 주석 추가
        if (tableData.getNote() != null && !tableData.getNote().isEmpty()) {
            Font noteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
            com.itextpdf.text.Paragraph noteParagraph = new com.itextpdf.text.Paragraph(tableData.getNote(), noteFont);
            noteParagraph.setSpacingBefore(5);
            document.add(noteParagraph);
        }
    }
    
    /**
     * DOCX 형식의 프레임워크 문서를 생성합니다.
     * 
     * @param title 문서 제목
     * @param frameworkId 프레임워크 ID
     * @return DOCX 바이트 배열
     * @throws IOException 문서 생성 중 오류 발생 시
     */
    private byte[] generateDocxDocument(String title, String frameworkId, List<EsgTableData> tableDataList) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // 제목 추가
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(title);
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("Arial");
            titleRun.addBreak();
            titleRun.addBreak();
            
            // 현재 날짜 추가
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("발행일: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            dateRun.setFontSize(12);
            dateRun.setFontFamily("Arial");
            dateRun.addBreak();
            dateRun.addBreak();
            
            // 본문 내용 추가
            XWPFParagraph contentParagraph = document.createParagraph();
            contentParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setText(frameworkId.toUpperCase() + " 프레임워크는 기업의 ESG 성과를 측정하고 보고하기 위한 국제 표준입니다. "
                    + "이 문서는 " + frameworkId.toUpperCase() + " 표준을 준수하여 ESG 보고서를 작성하는 방법을 안내합니다.");
            contentRun.setFontSize(12);
            contentRun.setFontFamily("Arial");
            contentRun.addBreak();
            contentRun.addBreak();
            
            // ESG 테이블 추가
            for (EsgTableData tableData : tableDataList) {
                addDocxTable(document, tableData);
                
                // 테이블 사이 공백 추가
                XWPFParagraph spaceParagraph = document.createParagraph();
                XWPFRun spaceRun = spaceParagraph.createRun();
                spaceRun.addBreak();
                spaceRun.addBreak();
            }
            
            // 안내 문구 추가
            XWPFParagraph noteParagraph = document.createParagraph();
            noteParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun noteRun = noteParagraph.createRun();
            noteRun.setText("※ 참고: 이 문서는 템플릿으로, 실제 보고서 작성 시 기업 특성에 맞게 조정이 필요합니다.");
            noteRun.setFontSize(12);
            noteRun.setFontFamily("Arial");
            
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * DOCX 문서에 ESG 테이블을 추가합니다.
     * 
     * @param document DOCX 문서
     * @param tableData ESG 테이블 데이터
     */
    private void addDocxTable(XWPFDocument document, EsgTableData tableData) {
        // 테이블 제목 추가
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(tableData.getTitle());
        titleRun.setBold(true);
        titleRun.setFontSize(12);
        
        if (tableData.getSubtitle() != null && !tableData.getSubtitle().isEmpty()) {
            titleRun.setText(" " + tableData.getSubtitle());
            titleRun.setBold(false);
            titleRun.setFontSize(10);
        }
        
        // GRI 정보 추가 (오른쪽 정렬)
        if (tableData.getGri() != null && !tableData.getGri().isEmpty()) {
            XWPFParagraph griParagraph = document.createParagraph();
            griParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun griRun = griParagraph.createRun();
            griRun.setText(tableData.getGri());
            griRun.setFontSize(10);
        }
        
        // 테이블 생성
        XWPFTable table = document.createTable(1 + tableData.getRowData().size(), tableData.getHeaders().size());
        table.setCellMargins(50, 50, 50, 50); // 셀 여백 설정
        
        // 테이블 너비 설정
        CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
        width.setType(STTblWidth.PCT);
        width.setW(BigInteger.valueOf(5000)); // 5000 = 100% 너비
        
        // 헤더 행 생성
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < tableData.getHeaders().size(); i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            // 배경색 설정 (회색)
            cell.setColor("F0F0F0");
            
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            
            XWPFRun run = paragraph.createRun();
            run.setText(tableData.getHeaders().get(i));
            run.setBold(true);
            run.setFontSize(10);
        }
        
        // 데이터 행 추가
        int rowIndex = 1;
        for (Map.Entry<String, List<String>> row : tableData.getRowData().entrySet()) {
            XWPFTableRow tableRow = table.getRow(rowIndex);
            
            // 행 이름 셀
            XWPFTableCell labelCell = tableRow.getCell(0);
            XWPFParagraph labelParagraph = labelCell.getParagraphs().get(0);
            labelParagraph.setAlignment(ParagraphAlignment.LEFT);
            
            XWPFRun labelRun = labelParagraph.createRun();
            labelRun.setText(row.getKey());
            labelRun.setFontSize(10);
            
            // 데이터 셀
            List<String> rowValues = row.getValue();
            for (int i = 1; i < tableData.getHeaders().size(); i++) {
                XWPFTableCell cell = tableRow.getCell(i);
                XWPFParagraph paragraph = cell.getParagraphs().get(0);
                paragraph.setAlignment(ParagraphAlignment.RIGHT);
                
                XWPFRun run = paragraph.createRun();
                String value = (i - 1 < rowValues.size()) ? rowValues.get(i - 1) : "";
                run.setText(value);
                run.setFontSize(10);
            }
            
            rowIndex++;
        }
        
        // 테이블 주석 추가
        if (tableData.getNote() != null && !tableData.getNote().isEmpty()) {
            XWPFParagraph noteParagraph = document.createParagraph();
            XWPFRun noteRun = noteParagraph.createRun();
            noteRun.setText(tableData.getNote());
            noteRun.setItalic(true);
            noteRun.setFontSize(8);
        }
    }
    
    /**
     * 회사별 맞춤형 보고서를 지정된 형식으로 생성합니다.
     * 
     * @param companyId 회사 ID
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식
     * @param companyName 회사명 (파일명에 사용)
     * @return 생성된 보고서 바이트 배열
     * @throws IOException 문서 생성 중 오류 발생 시
     */
    public byte[] generateCompanyReport(Long companyId, String frameworkId, String format, String companyName) 
            throws IOException {
        if (!"gri".equalsIgnoreCase(frameworkId)) {
            throw new IllegalArgumentException("현재 GRI 프레임워크만 지원합니다.");
        }
        
        if ("docx".equalsIgnoreCase(format)) {
            return reportGenerationService.generateEsgReportByCompanyId(companyId);
        } else if ("pdf".equalsIgnoreCase(format)) {
            // DOCX 보고서를 생성한 후 PDF로 변환
            try {
                byte[] docxReport = reportGenerationService.generateEsgReportByCompanyId(companyId);
                return convertDocxToPdf(docxReport, companyName);
            } catch (Exception e) {
                log.error("PDF 보고서 생성 중 오류 발생: {}", e.getMessage());
                throw new IOException("PDF 보고서 생성에 실패했습니다", e);
            }
        } else {
            throw new IllegalArgumentException("지원하지 않는 문서 형식: " + format);
        }
    }
    
    /**
     * DOCX 문서를 PDF로 변환합니다.
     * 
     * @param docxBytes DOCX 문서 바이트 배열
     * @param companyName 회사명 (PDF 메타데이터에 사용)
     * @return PDF 문서 바이트 배열
     * @throws IOException 변환 중 오류 발생 시
     * @throws DocumentException PDF 문서 생성 중 오류 발생 시
     */
    private byte[] convertDocxToPdf(byte[] docxBytes, String companyName) throws IOException, DocumentException {
        // 간단한 PDF 문서 생성 (실제 변환 대신 기본 템플릿 제공)
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 메타데이터 설정
            document.addTitle(companyName + " ESG 보고서");
            document.addAuthor("ESG Insight Board");
            document.addCreator("ESG Insight Board");
            document.addSubject("ESG 지속가능경영보고서");
            document.addKeywords("ESG, 지속가능경영, 보고서");
            
            // 제목 추가
            com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(
                    com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Paragraph titleParagraph = new com.itextpdf.text.Paragraph(
                    companyName + " ESG 지속가능경영보고서", titleFont);
            titleParagraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);
            
            // 변환 안내 메시지
            com.itextpdf.text.Font noteFont = com.itextpdf.text.FontFactory.getFont(
                    com.itextpdf.text.FontFactory.HELVETICA, 12);
            com.itextpdf.text.Paragraph noteParagraph = new com.itextpdf.text.Paragraph(
                "이 문서는 DOCX에서 변환된 PDF 파일입니다. " +
                "완전한 PDF 변환 기능은 업데이트 예정입니다. " +
                "보다 정확한 보고서는 DOCX 형식으로 다운로드하세요.", noteFont);
            noteParagraph.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            noteParagraph.setSpacingAfter(20);
            document.add(noteParagraph);
            
            // 현재 날짜
            com.itextpdf.text.Font dateFont = com.itextpdf.text.FontFactory.getFont(
                    com.itextpdf.text.FontFactory.HELVETICA, 10);
            com.itextpdf.text.Paragraph dateParagraph = new com.itextpdf.text.Paragraph(
                "생성일: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")), dateFont);
            dateParagraph.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(dateParagraph);
            
            document.close();
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 프레임워크 문서의 파일명을 생성합니다.
     * 
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식
     * @return 문서 파일명
     */
    public String getFrameworkDocumentFilename(String frameworkId, String format) {
        String formatDate = LocalDate.now().format(FILENAME_DATE_FORMATTER);
        return String.format("%s_프레임워크_가이드_%s.%s", 
                frameworkId.toUpperCase(), formatDate, format.toLowerCase());
    }
    
    /**
     * 회사별 보고서의 파일명을 생성합니다.
     * 
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식
     * @param companyName 회사명
     * @return 보고서 파일명
     */
    public String getCompanyReportFilename(String frameworkId, String format, String companyName) {
        String formatDate = LocalDate.now().format(FILENAME_DATE_FORMATTER);
        return String.format("%s_%s_지속가능경영보고서_%s.%s", 
                companyName, frameworkId.toUpperCase(), formatDate, format.toLowerCase());
    }
} 