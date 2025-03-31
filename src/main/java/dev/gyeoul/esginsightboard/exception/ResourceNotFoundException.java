package dev.gyeoul.esginsightboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외 클래스
 * <p>
 * 이 예외는 데이터베이스에서 검색한 엔티티가 존재하지 않을 때 사용됩니다.
 * 예를 들어, ID로 회사를 조회했으나 해당 ID의 회사가 없는 경우 발생합니다.
 * </p>
 * 
 * <p>
 * {@link ResponseStatus} 어노테이션이 있어 Spring MVC에서 자동으로
 * HTTP 404 응답으로 변환됩니다.
 * </p>
 * 
 * <p>
 * 사용 예시:
 * <pre>
 * Company company = companyRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("ID가 " + id + "인 회사를 찾을 수 없습니다."));
 * </pre>
 * </p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자
     */
    public ResourceNotFoundException() {
        super("요청한 리소스를 찾을 수 없습니다.");
    }

    /**
     * 오류 메시지를 지정하는 생성자
     *
     * @param message 오류 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 특정 리소스 유형과 식별자를 기반으로 오류 메시지를 생성하는 생성자
     *
     * @param resourceName 리소스 유형 (예: "회사", "GRI 데이터 항목")
     * @param fieldName 식별자 필드명 (예: "id", "code")
     * @param fieldValue 식별자 값
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s가 %s='%s'인 %s를 찾을 수 없습니다.", fieldName, fieldValue, resourceName));
    }

    /**
     * 오류 메시지와 원인 예외를 지정하는 생성자
     *
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 