package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.DocumentService;
import dev.gyeoul.esginsightboard.service.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 문서 및 보고서 다운로드 API 컨트롤러
 * <p>
 * 다양한 형식(PDF, DOCX)의 문서 및 ESG 보고서 다운로드 기능을 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "문서 및 보고서", description = "프레임워크 문서 및 회사별 ESG 보고서 다운로드 API")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final ReportGenerationService reportGenerationService;

    /**
     * GRI 문서를 다운로드합니다. - 프론트엔드 호환용 엔드포인트
     *
     * @param format 문서 형식 (pdf 또는 docx)
     * @return 요청된 형식의 GRI 문서
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Operation(summary = "GRI 문서 다운로드", 
              description = "GRI 프레임워크 문서를 다운로드합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "문서 다운로드 성공",
            content = @Content(mediaType = "application/octet-stream")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문서를 찾을 수 없음",
            content = @Content
        )
    })
    @GetMapping("/gri")
    public ResponseEntity<byte[]> downloadGriDocument(
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "pdf") String format) throws IOException {
        
        log.info("GRI 문서 다운로드 요청: 형식={}", format);
        
        // '/framework/gri' 엔드포인트로 리다이렉트
        return downloadFrameworkDocument("gri", format);
    }

    /**
     * 프레임워크 문서를 다운로드합니다.
     *
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식 (pdf 또는 docx)
     * @return 요청된 형식의 프레임워크 문서
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Operation(summary = "프레임워크 문서 다운로드", 
              description = "지정된 프레임워크(GRI, SASB, TCFD 등)의 문서를 다운로드합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "문서 다운로드 성공",
            content = @Content(mediaType = "application/octet-stream")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "문서를 찾을 수 없음",
            content = @Content
        )
    })
    @GetMapping("/framework/{frameworkId}")
    public ResponseEntity<byte[]> downloadFrameworkDocument(
            @Parameter(description = "프레임워크 ID (gri, sasb, tcfd 등)", required = true) 
            @PathVariable String frameworkId,
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "pdf") String format) throws IOException {
        
        log.info("프레임워크 문서 다운로드 요청: 프레임워크={}, 형식={}", frameworkId, format);
        
        // 지원하는 형식 확인
        if (!isFormatSupported(format)) {
            log.warn("지원하지 않는 형식 요청: {}", format);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"지원하지 않는 형식: " + format + "\",\"code\":\"UNSUPPORTED_FORMAT\"}").getBytes());
        }
        
        try {
            // 문서 생성
            byte[] document = documentService.getFrameworkDocument(frameworkId, format);
            
            if (document == null || document.length == 0) {
                log.warn("문서가 비어있습니다: 프레임워크={}, 형식={}", frameworkId, format);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(("{\"message\":\"문서가 비어 있습니다\",\"code\":\"EMPTY_DOCUMENT\"}").getBytes());
            }
            
            // 파일명 생성
            String filename = documentService.getFrameworkDocumentFilename(frameworkId, format);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            
            // 다운로드를 위한 Content-Disposition 헤더 설정 (지원 브라우저별 최적화)
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            
            headers.setContentLength(document.length);
            // 캐싱 설정 추가 (5분)
            headers.setCacheControl("max-age=300");
            // ETag 설정 (리소스 변경 감지용) - 따옴표로 감싸진 형태로 수정
            headers.setETag("\"" + String.valueOf(filename.hashCode()) + "\"");
            // 다운로드 진행률 추적용 헤더
            headers.set("X-Download-Options", "noopen");
            headers.set("X-Download-Progress", "0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(document);
        } catch (IOException e) {
            log.error("문서 다운로드 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"요청한 문서를 찾을 수 없습니다: " + e.getMessage() + "\",\"code\":\"DOCUMENT_NOT_FOUND\"}").getBytes());
        }
    }
    
    /**
     * 회사별 맞춤형 보고서를 다운로드합니다. 프레임워크 ID를 지정하여 사용합니다.
     *
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식 (pdf 또는 docx)
     * @return 회사 맞춤형 보고서
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Operation(summary = "회사별 맞춤형 보고서 다운로드", 
              description = "현재 인증된 사용자의 회사 데이터를 기반으로 맞춤형 보고서를 생성하여 다운로드합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "보고서 다운로드 성공",
            content = @Content(mediaType = "application/octet-stream")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "회사 정보 또는 문서를 찾을 수 없음",
            content = @Content
        )
    })
    @GetMapping("/company/{frameworkId}")
    public ResponseEntity<byte[]> downloadCompanyReportByFramework(
            @Parameter(description = "프레임워크 ID (gri, sasb, tcfd 등)", required = true) 
            @PathVariable String frameworkId,
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "docx") String format) throws IOException {
        
        log.info("회사별 맞춤형 보고서 다운로드 요청: 프레임워크={}, 형식={}", frameworkId, format);
        
        // 지원하는 형식 확인
        if (!isFormatSupported(format)) {
            log.warn("지원하지 않는 형식 요청: {}", format);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"지원하지 않는 형식: " + format + "\",\"code\":\"UNSUPPORTED_FORMAT\"}").getBytes());
        }
        
        try {
            // 현재 인증된 사용자에서 회사 정보를 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDto userDto = (UserDto) authentication.getPrincipal();
            
            Long companyId = userDto.getCompanyId();
            String companyName = userDto.getCompanyName();
            
            if (companyId == null) {
                log.warn("사용자 회사 정보 없음: 사용자 ID={}", userDto.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(("{\"message\":\"회사 정보가 없습니다\",\"code\":\"COMPANY_INFO_MISSING\"}").getBytes());
            }
            
            log.debug("회사 ID: {}, 회사명: {}", companyId, companyName);
            
            // 보고서 생성 서비스 호출
            byte[] report = documentService.generateCompanyReport(companyId, frameworkId, format, companyName);
            
            if (report == null || report.length == 0) {
                log.warn("생성된 보고서가 비어있습니다: 회사 ID={}, 프레임워크={}", companyId, frameworkId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(("{\"message\":\"보고서가 비어 있습니다\",\"code\":\"EMPTY_REPORT\"}").getBytes());
            }
            
            // 파일명 생성
            String filename = companyName.replaceAll("\\s+", "_") + "_" + frameworkId.toUpperCase() + "_Report_" 
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "." + format;
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            
            // 다운로드를 위한 Content-Disposition 헤더 설정 (지원 브라우저별 최적화)
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            
            headers.setContentLength(report.length);
            // 캐싱 설정 추가 (보고서는 캐싱하지 않음)
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");
            headers.set("Expires", "0");
            // 생성 타임스탬프 추가
            headers.set("X-Report-Generated", LocalDate.now().toString());
            // ETag 설정 - 따옴표로 감싸진 형태로 수정
            headers.setETag("\"" + String.valueOf(filename.hashCode()) + "\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report);
        } catch (Exception e) {
            log.error("회사 보고서 다운로드 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"보고서 생성 중 오류가 발생했습니다: " + e.getMessage() + "\",\"code\":\"REPORT_GENERATION_ERROR\"}").getBytes());
        }
    }
    
    /**
     * ESG 종합 보고서를 다운로드합니다.
     *
     * @param format 문서 형식 (pdf 또는 docx)
     * @return 종합 ESG 보고서
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Operation(summary = "ESG 종합 보고서 다운로드", 
              description = "현재 인증된 사용자의 회사에 대한 종합 ESG 보고서를 생성하여 다운로드합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "보고서 다운로드 성공",
            content = @Content(mediaType = "application/octet-stream")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "회사 정보 또는 문서를 찾을 수 없음",
            content = @Content
        )
    })
    @GetMapping("/company/report")
    public ResponseEntity<byte[]> downloadEsgReport(
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "docx") String format) throws IOException {
        
        log.info("ESG 종합 보고서 다운로드 요청: 형식={}", format);
        
        // 지원하는 형식 확인
        if (!isFormatSupported(format)) {
            log.warn("지원하지 않는 형식 요청: {}", format);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"지원하지 않는 형식: " + format + "\",\"code\":\"UNSUPPORTED_FORMAT\"}").getBytes());
        }
        
        try {
            // 현재 인증된 사용자에서 회사 정보를 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDto currentUser = (UserDto) authentication.getPrincipal();
            
            Long companyId = currentUser.getCompanyId();
            
            if (companyId == null) {
                log.warn("사용자 회사 정보 없음: 사용자 ID={}", currentUser.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(("{\"message\":\"회사 정보가 없습니다\",\"code\":\"COMPANY_INFO_MISSING\"}").getBytes());
            }
            
            // 보고서 생성 서비스 호출
            log.info("ESG 보고서 생성 요청: 회사 ID={}, 회사명={}", companyId, currentUser.getCompanyName());
            byte[] report = reportGenerationService.generateEsgReportByCompanyId(companyId);
            
            if (report == null || report.length == 0) {
                log.warn("생성된 ESG 보고서가 비어있습니다: 회사 ID={}", companyId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(("{\"message\":\"보고서가 비어 있습니다\",\"code\":\"EMPTY_REPORT\"}").getBytes());
            }
            
            // 파일명 생성
            String companyName = currentUser.getCompanyName() != null ? currentUser.getCompanyName() : "company";
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = companyName.replaceAll("\\s+", "_") + "_ESG_Report_" + today + ".docx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            
            // 다운로드를 위한 Content-Disposition 헤더 설정 (지원 브라우저별 최적화)
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            
            headers.setContentLength(report.length);
            // 캐싱 설정 추가 (보고서는 캐싱하지 않음)
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");
            headers.set("Expires", "0");
            // 생성 타임스탬프 추가
            headers.set("X-Report-Generated", today);
            // 다운로드 진행률 추적용 헤더
            headers.set("X-Download-Options", "noopen");
            headers.set("X-Download-Progress", "0");
            
            log.info("ESG 보고서 다운로드 준비 완료: 파일명={}, 크기={}bytes", filename, report.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report);
        } catch (Exception e) {
            log.error("ESG 보고서 다운로드 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"message\":\"보고서 생성 중 오류가 발생했습니다: " + e.getMessage() + "\",\"code\":\"REPORT_GENERATION_ERROR\"}").getBytes());
        }
    }
    
    /**
     * 지원하는 문서 형식인지 확인합니다.
     * 
     * @param format 문서 형식
     * @return 지원 여부
     */
    private boolean isFormatSupported(String format) {
        return "pdf".equalsIgnoreCase(format) || "docx".equalsIgnoreCase(format);
    }
    
    /**
     * 문서 형식에 따른 MediaType을 반환합니다.
     * 
     * @param format 문서 형식
     * @return MediaType
     */
    private MediaType getMediaType(String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return MediaType.APPLICATION_PDF;
        } else if ("docx".equalsIgnoreCase(format)) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
} 