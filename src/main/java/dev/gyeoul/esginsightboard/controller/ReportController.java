package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.ReportGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 로그인한 사용자의 회사에 대한 ESG 보고서를 생성하고 다운로드합니다.
     * 
     * @return 생성된 DOCX 보고서 파일
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    @Operation(
        summary = "내 회사 ESG 보고서 다운로드",
        description = "로그인한 사용자의 회사 데이터를 기반으로 GRI 프레임워크에 맞춘 ESG 보고서를 DOCX 형식으로 생성하여 다운로드합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "ESG 보고서 다운로드 성공",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 또는 토큰 없음",
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
    @GetMapping("/company")
    public ResponseEntity<byte[]> downloadCompanyReport() throws IOException {
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto currentUser = (UserDto) authentication.getPrincipal();
        
        // 인증되지 않은 경우 401 반환
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 사용자의 회사 ID로 보고서 생성
        Long companyId = currentUser.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 보고서 생성 서비스 호출
        byte[] report = reportGenerationService.generateEsgReportByCompanyId(companyId);
        
        // 파일명 생성 (회사명 + 현재 날짜)
        String companyName = currentUser.getCompanyName() != null ? 
                currentUser.getCompanyName() : "Company_" + companyId;
        
        String filename = "ESG_Report_" + companyName + "_" + 
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