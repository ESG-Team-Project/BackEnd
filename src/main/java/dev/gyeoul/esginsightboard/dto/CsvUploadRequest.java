package dev.gyeoul.esginsightboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * CSV 파일 업로드 요청을 위한 DTO(Data Transfer Object)
 * <p>
 * 이 클래스는 클라이언트가 CSV 파일을 업로드할 때 필요한 데이터를 담습니다.
 * 업로드할 CSV 파일을 포함합니다. 회사 정보 및 데이터 유형은 로그인 토큰에서 추출합니다.
 * </p>
 * 
 * <p>
 * 주로 다음과 같은 상황에서 사용됩니다:
 * <ul>
 *   <li>컨트롤러에서 클라이언트 요청을 받아 서비스 계층으로 전달</li>
 *   <li>서비스 계층에서 CSV 파일 처리 작업 수행</li>
 * </ul>
 * </p>
 * 
 * <p>
 * 다음 예시와 같이 빌더 패턴을 사용하여 인스턴스를 생성할 수 있습니다:
 * <pre>
 * CsvUploadRequest request = CsvUploadRequest.builder()
 *     .file(multipartFile)
 *     .build();
 * </pre>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CSV 파일 업로드 요청 데이터")
public class CsvUploadRequest {
    
    /**
     * 업로드할 CSV 파일
     * <p>
     * MultipartFile 형태로 클라이언트로부터 전송된 CSV 파일입니다.
     * 이 파일은 나중에 CsvImportService에서 처리됩니다.
     * </p>
     */
    @NotNull(message = "CSV 파일은 필수입니다")
    @Schema(description = "업로드할 CSV 파일", required = true)
    private MultipartFile file;
} 