package dev.gyeoul.esginsightboard.exception;

/**
 * CSV 파일 처리 중 발생하는 오류를 나타내는 예외 클래스
 * <p>
 * 이 예외는 CSV 파일 업로드, 파싱, 데이터 변환 과정에서 발생하는 
 * 모든 오류를 처리하기 위해 사용됩니다.
 * </p>
 * 
 * <p>
 * 사용 예시:
 * <pre>
 * if (csvFile.isEmpty()) {
 *     throw new CsvProcessingException("CSV 파일이 비어 있습니다.");
 * }
 * </pre>
 * </p>
 */
public class CsvProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자
     */
    public CsvProcessingException() {
        super("CSV 파일 처리 중 오류가 발생했습니다.");
    }

    /**
     * 오류 메시지를 지정하는 생성자
     *
     * @param message 오류 메시지
     */
    public CsvProcessingException(String message) {
        super(message);
    }

    /**
     * 오류 메시지와 원인 예외를 지정하는 생성자
     *
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public CsvProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인 예외만 지정하는 생성자
     *
     * @param cause 원인 예외
     */
    public CsvProcessingException(Throwable cause) {
        super("CSV 파일 처리 중 오류가 발생했습니다: " + cause.getMessage(), cause);
    }
} 