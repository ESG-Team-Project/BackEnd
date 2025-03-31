package dev.gyeoul.esginsightboard.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "gri_indicator_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsgIndicator {
    public enum VerificationStatus {
        /** 미검증 상태 */
        UNVERIFIED("미검증"),

        /** 검증 진행 중 상태 */
        IN_PROGRESS("검증중"),

        /** 검증 완료 상태 */
        VERIFIED("검증완료"),

        /** 검증 실패 상태 */
        FAILED("검증실패");

        private final String displayName;

        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * 표시 이름으로 열거형 값을 찾습니다.
         *
         * @param displayName 표시 이름
         * @return 해당하는 VerificationStatus 또는 없으면 UNVERIFIED
         */
        public static EsgIndicator.VerificationStatus fromDisplayName(String displayName) {
            for (EsgIndicator.VerificationStatus status : values()) {
                if (status.displayName.equals(displayName)) {
                    return status;
                }
            }
            return UNVERIFIED;
        }
    }


    @Column(nullable = false)
    private String standardCode; //표준코드

    @Column(nullable = false)
    private String disclosureCode; //공시코드

    @Column(nullable = false)
    private String disclosureTitle; //공시 항목 제목

    @Column(length = 1000)
    private String disclosureValue; //공시항목 텍스트 value

    @Column(length = 2000)
    private String description; //데이터 추가 설명

    private Double numericValue; //정량 수치

    private String unit; //수치 단위

    private LocalDate reportingPeriodStart; //보고 기간 시작일

    private LocalDate reportingPeriodEnd; //보고 기간 종료일

//    private String verificationStatus; //데이터 검증 상태

    private String verificationProvider; //검증 수행한 기관 or 제공자

    @Column(nullable = false)
    private String category; //ESG카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; //속한 회사

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; //엔티티 생성 일시

    @Column(nullable = false)
    private LocalDateTime updatedAt; //엔티티 마지막 수정 일시
}

