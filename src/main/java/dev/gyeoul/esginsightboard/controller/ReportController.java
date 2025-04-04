package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.service.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ESG 보고서 생성 및 다운로드를 위한 REST API 컨트롤러
 * <p>
 * 이 컨트롤러는 회사별 ESG 데이터를 기반으로 생성한 GRI 보고서를 다운로드할 수 있는 엔드포인트를 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "ESG 보고서", description = "ESG 보고서 생성 및 다운로드 API")
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    /**
     * 회사 ID를 기반으로 ESG 보고서를 생성하고 다운로드합니다.
     * 
     * @param companyId 회사 ID
     * @return 생성된 DOCX 보고서 파일
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    @Operation(
        summary = "ESG 보고서 다운로드",
        description = "회사 ID를 기반으로 GRI 프레임워크에 맞춘 ESG 보고서를 DOCX 형식으로 생성하여 다운로드합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "ESG 보고서 다운로드 성공",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "회사 정보를 찾을 수 없음",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "보고서 생성 중 오류 발생 (ESG 데이터 없음)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content
        )
    })
    @GetMapping("/esg/{companyId}")
    public ResponseEntity<byte[]> downloadEsgReport(
            @Parameter(description = "보고서를 생성할 회사의 ID", required = true)
            @PathVariable Long companyId) throws IOException {
        // 보고서 생성 서비스 호출
        byte[] report = reportGenerationService.generateEsgReportByCompanyId(companyId);
        
        // 파일명 생성 (회사 ID + 현재 날짜)
        String filename = "ESG_Report_Company_" + companyId + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".docx";
        
        // 한글 파일명을 위한 인코딩
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(report.length);
        
        // 응답 반환
        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }
} 