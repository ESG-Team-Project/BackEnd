package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.DocumentService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 문서 다운로드 API 컨트롤러
 * <p>
 * 다양한 형식(PDF, DOCX)의 문서 다운로드 기능을 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "문서 다운로드", description = "프레임워크 문서 및 회사별 보고서 다운로드 API")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

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
    @GetMapping("/{frameworkId}")
    public ResponseEntity<byte[]> downloadFrameworkDocument(
            @Parameter(description = "프레임워크 ID (gri, sasb, tcfd 등)", required = true) 
            @PathVariable String frameworkId,
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "pdf") String format) throws IOException {
        
        log.debug("프레임워크 문서 다운로드 요청: 프레임워크={}, 형식={}", frameworkId, format);
        
        // 지원하는 형식 확인
        if (!isFormatSupported(format)) {
            return ResponseEntity.badRequest().build();
        }
        
        // 문서 생성
        byte[] document = documentService.getFrameworkDocument(frameworkId, format);
        
        // 파일명 생성
        String filename = documentService.getFrameworkDocumentFilename(frameworkId, format);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(document.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }
    
    /**
     * 회사별 맞춤형 보고서를 다운로드합니다.
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
    public ResponseEntity<byte[]> downloadCompanyReport(
            @Parameter(description = "프레임워크 ID (gri, sasb, tcfd 등)", required = true) 
            @PathVariable String frameworkId,
            @Parameter(description = "문서 형식 (pdf 또는 docx)", required = true) 
            @RequestParam(defaultValue = "docx") String format) throws IOException {
        
        log.debug("회사별 맞춤형 보고서 다운로드 요청: 프레임워크={}, 형식={}", frameworkId, format);
        
        // 지원하는 형식 확인
        if (!isFormatSupported(format)) {
            return ResponseEntity.badRequest().build();
        }
        
        // 현재 인증된 사용자에서 회사 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = (UserDto) authentication.getPrincipal();
        
        Long companyId = userDto.getCompanyId();
        String companyName = userDto.getCompanyName();
        
        if (companyId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        log.debug("회사 ID: {}, 회사명: {}", companyId, companyName);
        
        // 보고서 생성
        byte[] report = documentService.generateCompanyReport(companyId, frameworkId, format, companyName);
        
        // 파일명 생성
        String filename = documentService.getCompanyReportFilename(frameworkId, format, companyName);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(report.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
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