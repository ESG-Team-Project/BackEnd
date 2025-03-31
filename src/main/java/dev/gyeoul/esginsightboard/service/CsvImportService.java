package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.CsvUploadRequest;
import dev.gyeoul.esginsightboard.dto.CsvUploadResponse;
import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.exception.CsvProcessingException;
import dev.gyeoul.esginsightboard.exception.ResourceNotFoundException;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * CSV 파일을 처리하고 데이터베이스에 저장하는 서비스
 * <p>
 * 이 서비스는 다음과 같은 기능을 제공합니다:
 * <ul>
 *   <li>CSV 파일 파싱 및 유효성 검사</li>
 *   <li>GRI 데이터 처리 및 저장</li>
 *   <li>CSV 데이터를 엔티티로 변환</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    // 상수 정의
    /**
     * 지원하는 데이터 유형 - GRI
     */
    public static final String DATA_TYPE_GRI = "gri";
    
    /**
     * 샘플 데이터의 최대 개수
     */
    private static final int MAX_SAMPLE_SIZE = 5;
    
    /**
     * 날짜 형식 (yyyy-MM-dd)
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 날짜 형식 포매터
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
    /**
     * GRI 데이터의 ESG 카테고리 식별 키워드
     */
    private static final Map<String, String> CATEGORY_KEYWORDS;
    
    // 정적 초기화 블록을 사용하여 CATEGORY_KEYWORDS 초기화
    static {
        Map<String, String> keywords = new HashMap<>();
        // 환경(Environment) 관련 키워드
        keywords.put("환경", "E");
        keywords.put("에너지", "E");
        keywords.put("배출", "E");
        keywords.put("용수", "E");
        keywords.put("폐기물", "E");
        keywords.put("생물다양성", "E");
        
        // 사회(Social) 관련 키워드
        keywords.put("사회", "S");
        keywords.put("인권", "S");
        keywords.put("지역사회", "S");
        keywords.put("노동", "S");
        keywords.put("안전", "S");
        keywords.put("보건", "S");
        keywords.put("다양성", "S");
        keywords.put("포용성", "S");
        
        // 지배구조(Governance) 관련 키워드
        keywords.put("지배구조", "G");
        keywords.put("윤리", "G");
        keywords.put("투명성", "G");
        keywords.put("반부패", "G");
        keywords.put("이사회", "G");
        keywords.put("준법", "G");
        
        CATEGORY_KEYWORDS = Collections.unmodifiableMap(keywords);
    }
    
    // 필요한 리포지토리 주입
    private final GriDataItemRepository griDataItemRepository;
    private final CompanyRepository companyRepository;

    /**
     * CSV 파일을 파싱하고 데이터베이스에 저장
     * <p>
     * 업로드된 CSV 파일을 GRI 데이터로 처리합니다.
     * 회사 정보는 로그인한 사용자의 정보에서 가져옵니다.
     * </p>
     *
     * @param request CSV 업로드 요청 데이터 (파일 포함)
     * @param user 로그인한 사용자 정보
     * @return CSV 업로드 처리 결과
     */
    @Transactional
    public CsvUploadResponse processCsvFile(CsvUploadRequest request, UserDto user) {
        log.info("CSV 파일 처리 시작. 사용자: {}", user.getEmail());

        // 입력값 유효성 검사
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new CsvProcessingException("업로드된 파일이 없거나 빈 파일입니다.");
        }

        // 사용자 정보 확인
        if (user == null || user.getCompanyName() == null) {
            throw new CsvProcessingException("사용자 정보 또는 회사 정보가 유효하지 않습니다.");
        }

        // 회사 존재 여부 확인
        Company company = companyRepository.findByName(user.getCompanyName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "회사명 '" + user.getCompanyName() + "'에 해당하는 회사를 찾을 수 없습니다."));

        try {
            // CSV 파일 읽기
            List<Map<String, String>> csvData = readCsvFile(request.getFile());
            
            if (csvData.isEmpty()) {
                throw new CsvProcessingException("CSV 파일에 데이터가 없습니다.");
            }

            // GRI 데이터 처리 (현재는 GRI 데이터만 지원)
            return processGriData(csvData, company.getId());

        } catch (IOException e) {
            log.error("CSV 파일 읽기 중 오류 발생", e);
            throw new CsvProcessingException("CSV 파일을 읽을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("CSV 처리 중 예상치 못한 오류 발생", e);
            throw new CsvProcessingException("CSV 처리 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * CSV 파일을 읽고 맵 리스트로 변환
     * <p>
     * 이 메서드는 업로드된 CSV 파일을 읽어서 각 행을 헤더를 키로 하는 맵으로 변환합니다.
     * Apache Commons CSV 라이브러리를 사용하여 CSV 파싱을 처리합니다.
     * </p>
     *
     * @param file 업로드된 CSV 파일
     * @return 각 행의 데이터를 담은 맵 리스트 (키: 헤더명, 값: 셀 데이터)
     * @throws IOException 파일 읽기 중 오류 발생 시
     * @throws CsvProcessingException CSV 파싱 중 오류 발생 시
     */
    private List<Map<String, String>> readCsvFile(MultipartFile file) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        // UTF-8 인코딩으로 파일 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            
            // CSV 파서 설정 (첫 줄은 헤더로 사용)
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build());

            // 각 레코드(행)를 맵으로 변환하여 리스트에 추가
            for (CSVRecord record : csvParser) {
                Map<String, String> rowData = new HashMap<>();
                csvParser.getHeaderNames().forEach(header -> 
                    rowData.put(header, record.get(header))
                );
                result.add(rowData);
            }
        } catch (IllegalArgumentException e) {
            log.error("CSV 파싱 중 형식 오류 발생", e);
            throw new CsvProcessingException("CSV 형식이 올바르지 않습니다: " + e.getMessage());
        }

        return result;
    }
    
    /**
     * GRI 데이터를 처리하여 데이터베이스에 저장
     * <p>
     * CSV에서 읽은 데이터를 GriDataItem 엔티티로 변환하고 데이터베이스에 저장합니다.
     * 회사 ID가 제공된 경우, 해당 회사와 데이터를 연결합니다.
     * </p>
     *
     * @param csvData CSV 데이터 맵 목록 (헤더를 키로 하는 맵 목록)
     * @param companyId 회사 ID (null일 수 있음)
     * @return 처리 결과
     */
    private CsvUploadResponse processGriData(List<Map<String, String>> csvData, Long companyId) {
        log.info("GRI 데이터 처리 시작. 데이터 행 수: {}", csvData.size());
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ID가 " + companyId + "인 회사를 찾을 수 없습니다."));

        int processedCount = 0;
        int errorCount = 0;
        List<String> errorMessages = new ArrayList<>();

        // 각 행 처리
        for (int i = 0; i < csvData.size(); i++) {
            try {
                Map<String, String> row = csvData.get(i);
                GriDataItem dataItem = convertRowToGriDataItem(row);
                dataItem.setCompany(company);
                
                // 데이터베이스에 저장
                griDataItemRepository.save(dataItem);
                processedCount++;
                
            } catch (Exception e) {
                log.error("{}번째 행 처리 중 오류 발생: {}", i + 1, e.getMessage());
                errorMessages.add("행 " + (i + 1) + ": " + e.getMessage());
                errorCount++;
            }
        }

        log.info("GRI 데이터 처리 완료. 성공: {}, 실패: {}", processedCount, errorCount);
        
        // 처리 결과 반환
        return CsvUploadResponse.builder()
                .success(errorCount == 0)
                .totalRows(csvData.size())
                .processedRows(processedCount)
                .errorRows(errorCount)
                .errorMessages(errorMessages)
                .build();
    }
    
    /**
     * CSV 행 데이터를 GriDataItem 엔티티로 변환
     * <p>
     * CSV 행의 데이터를 적절히 변환하여 GriDataItem 엔티티를 생성합니다.
     * 날짜, 숫자 등의 데이터 타입은 적절히 변환됩니다.
     * </p>
     *
     * @param row CSV 데이터 행 (헤더를 키로 하는 맵)
     * @return 변환된 GriDataItem 엔티티
     */
    private GriDataItem convertRowToGriDataItem(Map<String, String> row) {
        // 필수 필드 검증
        if (!row.containsKey("standardCode") || row.get("standardCode").trim().isEmpty()) {
            throw new CsvProcessingException("GRI 표준 코드가 누락되었습니다.");
        }
        
        if (!row.containsKey("disclosureTitle") || row.get("disclosureTitle").trim().isEmpty()) {
            throw new CsvProcessingException("공시 제목이 누락되었습니다.");
        }

        // GRI 데이터 항목 생성을 위한 변수
        String standardCode = row.get("standardCode");
        String disclosureCode = row.getOrDefault("disclosureCode", "");
        String disclosureTitle = row.get("disclosureTitle");
        String disclosureValue = row.getOrDefault("disclosureValue", "");
        String description = row.getOrDefault("description", "");
        Double numericValue = null;
        String unit = row.getOrDefault("unit", "");
        LocalDate reportingPeriodStart = null;
        LocalDate reportingPeriodEnd = null;
        String verificationStatus = row.getOrDefault("verificationStatus", "미검증");
        String verificationProvider = row.getOrDefault("verificationProvider", "");
        
        // 숫자 값 처리
        if (row.containsKey("numericValue") && !row.get("numericValue").trim().isEmpty()) {
            try {
                numericValue = parseNumericValue(row.get("numericValue"));
            } catch (NumberFormatException e) {
                throw new CsvProcessingException("숫자 값 형식이 올바르지 않습니다: " + row.get("numericValue"));
            }
        }
        
        // 보고 기간 처리
        if (row.containsKey("reportingPeriodStart") && !row.get("reportingPeriodStart").trim().isEmpty()) {
            try {
                reportingPeriodStart = parseDate(row.get("reportingPeriodStart"));
            } catch (DateTimeParseException e) {
                throw new CsvProcessingException("보고 기간 시작일 형식이 올바르지 않습니다. 올바른 형식은 " + 
                        DATE_FORMAT + "입니다: " + row.get("reportingPeriodStart"));
            }
        }
        
        if (row.containsKey("reportingPeriodEnd") && !row.get("reportingPeriodEnd").trim().isEmpty()) {
            try {
                reportingPeriodEnd = parseDate(row.get("reportingPeriodEnd"));
            } catch (DateTimeParseException e) {
                throw new CsvProcessingException("보고 기간 종료일 형식이 올바르지 않습니다. 올바른 형식은 " + 
                        DATE_FORMAT + "입니다: " + row.get("reportingPeriodEnd"));
            }
        }
        
        // 보고 기간 유효성 검사
        if (reportingPeriodStart != null && reportingPeriodEnd != null 
                && reportingPeriodStart.isAfter(reportingPeriodEnd)) {
            throw new CsvProcessingException("보고 기간 시작일이 종료일보다 이후입니다: " + 
                    reportingPeriodStart + " > " + reportingPeriodEnd);
        }
        
        // ESG 카테고리 자동 결정
        String category = determineEsgCategory(standardCode, disclosureTitle);
        
        // GriDataItem 엔티티 생성 및 반환
        return GriDataItem.builder()
                .standardCode(standardCode)
                .disclosureCode(disclosureCode)
                .disclosureTitle(disclosureTitle)
                .disclosureValue(disclosureValue)
                .description(description)
                .numericValue(numericValue)
                .unit(unit)
                .reportingPeriodStart(reportingPeriodStart)
                .reportingPeriodEnd(reportingPeriodEnd)
                .verificationStatus(verificationStatus)
                .verificationProvider(verificationProvider)
                .category(category)
                .build();
    }
    
    /**
     * 날짜 문자열을 LocalDate로 파싱
     *
     * @param dateStr 날짜 문자열 (yyyy-MM-dd 형식)
     * @return 파싱된 LocalDate 객체
     * @throws DateTimeParseException 날짜 형식이 올바르지 않을 경우
     */
    private LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    /**
     * 숫자 값 문자열을 Double로 변환
     * <p>
     * 이 메서드는 숫자 값에서 쉼표를 제거하고 Double로 변환합니다.
     * 예: "1,234.56" -> 1234.56
     * </p>
     *
     * @param value 변환할 숫자 문자열
     * @return 변환된 Double 값
     * @throws NumberFormatException 숫자 형식이 올바르지 않을 경우
     */
    private Double parseNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        // 쉼표 제거 후 숫자로 변환
        String cleanValue = value.replaceAll(",", "");
        return Double.parseDouble(cleanValue);
    }
    
    /**
     * 표준 코드와 공시 제목을 기반으로 ESG 카테고리(E, S, G) 결정
     * <p>
     * 이 메서드는 GRI 표준 코드와 공시 제목을 분석하여 
     * 환경(E), 사회(S), 지배구조(G) 중 어떤 카테고리에 속하는지 결정합니다.
     * </p>
     *
     * @param standardCode GRI 표준 코드 (예: GRI 302)
     * @param disclosureTitle 공시 제목
     * @return ESG 카테고리 (E, S, G 중 하나)
     */
    private String determineEsgCategory(String standardCode, String disclosureTitle) {
        if (standardCode == null && disclosureTitle == null) {
            return "기타";
        }
        
        // GRI 코드 기반 카테고리 결정
        if (standardCode != null) {
            // 환경 관련 GRI 표준
            if (standardCode.matches("(?i).*30[1-8].*")) {
                return "E";
            }
            
            // 사회 관련 GRI 표준
            if (standardCode.matches("(?i).*4[0-1][0-9].*")) {
                return "S";
            }
            
            // 지배구조 관련 GRI 표준
            if (standardCode.matches("(?i).*2[0-9][0-9].*")) {
                return "G";
            }
        }
        
        // 제목 키워드 기반 카테고리 결정
        if (disclosureTitle != null) {
            for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
                if (disclosureTitle.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        
        // 기본값
        return "기타";
    }
} 