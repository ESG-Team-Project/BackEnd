package dev.gyeoul.esginsightboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * CSV 파일 업로드 요청에 대한 응답 DTO
 * <p>
 * 클라이언트에게 CSV 파일 처리 결과를 반환하기 위한 데이터 구조입니다.
 * 성공/실패 여부, 처리된 레코드 수, 오류 메시지 등의 정보를 포함합니다.
 * </p>
 */
@Data
@Builder
public class CsvUploadResponse {

    /**
     * CSV 처리 성공 여부
     * <p>
     * 전체 처리 과정이 성공적으로 완료되었는지를 나타냅니다.
     * 일부 레코드에 오류가 있어도 전체 프로세스가 완료되었다면 true일 수 있습니다.
     * </p>
     */
    private boolean success;

    /**
     * 사용자에게 표시할 메시지
     * <p>
     * 처리 결과에 대한 간략한 설명입니다. 성공 또는 실패 이유를 설명합니다.
     * </p>
     */
    private String message;

    /**
     * 처리된 총 레코드 수
     * <p>
     * 성공적으로 처리되어 데이터베이스에 저장된 레코드의 수입니다.
     * </p>
     * @deprecated {@link #processedRows} 사용을 권장
     */
    @Deprecated
    private Integer recordCount;

    /**
     * 오류 메시지
     * <p>
     * 처리 중 발생한 오류에 대한 상세 메시지입니다.
     * </p>
     * @deprecated {@link #errorMessages} 사용을 권장
     */
    @Deprecated
    private String errorMessage;

    /**
     * 처리된 샘플 데이터
     * <p>
     * 처리된 데이터의 일부 샘플을 클라이언트에게 보여주기 위한 목적으로 사용됩니다.
     * 내용 확인이나 디버깅에 유용합니다.
     * </p>
     */
    private List<Map<String, Object>> dataSample;

    /**
     * CSV 파일의 총 행 수
     * <p>
     * 처리를 시도한 CSV 파일의 전체 행 수입니다(헤더 제외).
     * </p>
     */
    private Integer totalRows;

    /**
     * 성공적으로 처리된 행 수
     * <p>
     * 오류 없이 성공적으로 처리되어 데이터베이스에 저장된 행의 수입니다.
     * </p>
     */
    private Integer processedRows;

    /**
     * 오류가 발생한 행 수
     * <p>
     * 처리 중 오류가 발생하여 데이터베이스에 저장되지 않은 행의 수입니다.
     * </p>
     */
    private Integer errorRows;

    /**
     * 처리 중 발생한 오류 메시지 목록
     * <p>
     * 각 행별로 발생한 오류 메시지를 담고 있습니다.
     * 형식: "행 5: 숫자 값 형식이 올바르지 않습니다."
     * </p>
     */
    private List<String> errorMessages;
} 