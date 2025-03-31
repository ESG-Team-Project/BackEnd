package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.CsvUploadRequest;
import dev.gyeoul.esginsightboard.dto.CsvUploadResponse;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 데이터 임포트 관련 API 엔드포인트를 제공하는 컨트롤러
 * <p>
 * 이 컨트롤러는 CSV 파일 업로드, 샘플 다운로드, 사용 가이드 제공 등
 * ESG 데이터 임포트 기능에 관련된 모든 API 엔드포인트를 처리합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/data-import")
@RequiredArgsConstructor
@Tag(name = "Data Import API", description = "CSV 파일을 통한 ESG 데이터 임포트 및 관련 기능")
public class DataImportController {
    private static final String UPLOAD_DIR = "csv";
    /**
     * 컨텐츠 타입 상수 - CSV
     */
    private static final String CONTENT_TYPE_CSV = "text/csv";

    /**
     * 파일 이름 상수 - 샘플 GRI 데이터
     */
    private static final String SAMPLE_GRI_FILENAME = "sample-gri-data.csv";

    /**
     * 서비스 의존성
     */
    private final CsvImportService csvImportService;

    /**
     * CSV 파일 업로드 및 데이터 임포트 처리
     * <p>
     * 이 엔드포인트는 클라이언트로부터 CSV 파일을 받아 특정 회사의 ESG 데이터로 처리합니다.
     * 데이터 유형에 따라 적절한 엔티티로 변환하고 데이터베이스에 저장합니다.
     * </p>
     *
     * <p>
     * 요청 예시:
     * <pre>
     * curl -X POST http://localhost:8080/api/data-import/csv \
     *   -F "file=@sample_gri_data.csv" \
     *   -F "companyId=1" \
     *   -F "dataType=GRI"
     * </pre>
     * </p>
     *
     * @param request CSV 파일 업로드 요청 데이터
     * @return 처리 결과를 담은 응답 객체
     */
    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @Operation(
            summary = "CSV 파일 업로드 및 처리",
            description = "CSV 파일을 업로드하여 ESG 데이터로 처리하고 데이터베이스에 저장합니다. " +
                    "파일 및 필수 정보에 대한 유효성을 검증합니다. 회사 정보는 로그인 토큰에서 추출합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV 파일 처리 성공",
                    content = @Content(schema = @Schema(implementation = CsvUploadResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "success": false,
                                      "message": "입력값 유효성 검증에 실패했습니다",
                                      "errors": {
                                        "file": "CSV 파일은 필수입니다"
                                      },
                                      "error": "VALIDATION_FAILED"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "회사를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CsvUploadResponse> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {

        // 로그인 토큰에서 사용자 정보 가져오기
        UserDto user = (UserDto) httpRequest.getAttribute("user");
        log.info("user: {}", user);

        // 인증 확인
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("CSV 파일 업로드 요청: 파일명={}, 크기={}, 사용자={}",
                file.getOriginalFilename(),
                file.getSize(),
                user.getEmail()
        );

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 서비스 호출 및 결과 반환
            CsvUploadRequest request = new CsvUploadRequest(file);
            CsvUploadResponse response = csvImportService.processCsvFile(request, user);

            // 임시 파일 삭제
            // Files.deleteIfExists(filePath);

            if (response.isSuccess()) {
                log.info("CSV 파일 처리 성공: 처리된 행 수={}", response.getProcessedRows());
                return ResponseEntity.ok(response);
            } else {
                log.warn("CSV 파일 처리 중 오류 발생: {}", response.getErrorMessages());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("CSV 파일 처리 중 예외 발생", e);

            // 예외 상황에 대한 응답 생성
            CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                    .success(false)
                    .message("CSV 파일 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 샘플 GRI 데이터 CSV 파일 다운로드
     * <p>
     * 이 엔드포인트는 사용자가 CSV 파일 형식을 이해하고 올바르게 작성할 수 있도록
     * 샘플 GRI 데이터 CSV 파일을 제공합니다.
     * </p>
     *
     * <p>
     * 요청 예시:
     * <pre>
     * curl -X GET http://localhost:8080/api/data-import/sample-gri-data -O
     * </pre>
     * </p>
     *
     * @return 샘플 CSV 파일 리소스
     */
    @GetMapping("/sample-gri-data")
    @Operation(
            summary = "샘플 GRI 데이터 CSV 파일 다운로드",
            description = "사용자가 참고할 수 있는 샘플 GRI 데이터 CSV 파일을 다운로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "샘플 파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Resource> downloadSampleGriData() {
        try {
            // 클래스패스에서 샘플 CSV 파일 로드
            ClassPathResource resource = new ClassPathResource("static/samples/sample-gri-data.csv");

            // 파일 존재 여부 확인
            if (!resource.exists()) {
                log.error("샘플 GRI 데이터 CSV 파일을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }

            // 파일 다운로드를 위한 HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE_CSV));
            headers.setContentDispositionFormData("attachment", SAMPLE_GRI_FILENAME);

            log.info("샘플 GRI 데이터 CSV 파일 다운로드 요청 처리 완료");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("샘플 GRI 데이터 CSV 파일 다운로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CSV 파일 업로드 사용 가이드 페이지 표시
     * <p>
     * 이 엔드포인트는 사용자에게 CSV 파일 업로드 및 데이터 임포트에 대한
     * 상세한 사용 방법을 안내하는 HTML 페이지를 제공합니다.
     * </p>
     *
     * <p>
     * 요청 예시:
     * <pre>
     * curl -X GET http://localhost:8080/api/data-import/guide
     * </pre>
     * </p>
     *
     * @return 사용 가이드 HTML 페이지의 뷰 이름
     */
    @GetMapping("/guide")
    @Operation(
            summary = "CSV 파일 업로드 사용 가이드",
            description = "CSV 파일 업로드 및 ESG 데이터 임포트에 관한 사용 가이드 페이지를 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가이드 페이지 로드 성공")
    })
    public String showUsageGuide() {
        log.info("CSV 파일 업로드 사용 가이드 페이지 요청");
        // 'usage-guide'라는 이름의 템플릿을 반환
        // (src/main/resources/templates/usage-guide.html)
        return "usage-guide";
    }

    /**
     * CSV 템플릿 파일 다운로드
     * <p>
     * 이 엔드포인트는 사용자가 자신의 데이터를 입력할 수 있는 빈 CSV 템플릿 파일을 제공합니다.
     * 템플릿은 데이터 유형에 따라 다른 구조를 가질 수 있습니다.
     * </p>
     *
     * <p>
     * 요청 예시:
     * <pre>
     * curl -X GET http://localhost:8080/api/data-import/template?type=GRI -O
     * </pre>
     * </p>
     *
     * @param type 템플릿 유형 (예: "GRI")
     * @return CSV 템플릿 파일
     */
    @GetMapping("/template")
    @Operation(
            summary = "CSV 템플릿 파일 다운로드",
            description = "사용자가 데이터를 입력할 수 있는 빈 CSV 템플릿 파일을 다운로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "템플릿 다운로드 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 템플릿 유형"),
            @ApiResponse(responseCode = "404", description = "템플릿 파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Resource> downloadTemplate(
            @Parameter(description = "템플릿 유형 (예: \"GRI\")", required = true)
            @RequestParam("type") String type) {

        try {
            // 템플릿 유형 확인 및 파일명 결정
            String templateFileName;
            switch (type.toUpperCase()) {
                case "GRI":
                    templateFileName = "gri-template.csv";
                    break;
                default:
                    log.warn("지원하지 않는 템플릿 유형: {}", type);
                    return ResponseEntity.badRequest().build();
            }

            // 클래스패스에서 템플릿 파일 로드
            ClassPathResource resource = new ClassPathResource("static/templates/" + templateFileName);

            // 파일 존재 여부 확인
            if (!resource.exists()) {
                log.error("템플릿 파일을 찾을 수 없습니다: {}", templateFileName);
                return ResponseEntity.notFound().build();
            }

            // 파일 다운로드를 위한 HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", templateFileName);

            log.info("CSV 템플릿 파일 다운로드 요청 처리 완료: {}", templateFileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("CSV 템플릿 파일 다운로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 