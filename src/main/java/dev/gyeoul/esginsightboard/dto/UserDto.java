package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 담는 DTO 클래스
 * <p>
 * 이 클래스는 사용자 정보를 API 응답으로 제공하거나 서비스 계층 간에 전송하는 데 사용됩니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보")
public class UserDto {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "부서", example = "ESG 관리팀")
    private String department;
    
    @Schema(description = "직위", example = "팀장")
    private String position;
    
    @Schema(description = "회사명", example = "(주)환경기술")
    private String companyName;
    
    @Schema(description = "대표자명", example = "김대표")
    private String ceoName;
    
    @Schema(description = "회사 코드", example = "ECO2023")
    private String companyCode;
    
    @Schema(description = "회사 전화번호", example = "02-1234-5678")
    private String companyPhoneNumber;
    
    @Schema(description = "개인 전화번호", example = "010-1234-5678")
    private String phoneNumber;
    
    @Schema(description = "회사 ID", example = "1")
    private Long companyId;
    
    @Schema(description = "계정 생성일시", example = "2025-01-01T09:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "계정 수정일시", example = "2025-04-01T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * User 엔티티를 UserDto로 변환합니다.
     */
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .companyName(user.getCompany().getName())
                .ceoName(user.getCompany().getCeoName())
                .companyCode(user.getCompany().getCompanyCode())
                .companyPhoneNumber(user.getCompany().getCompanyPhoneNumber())
                .phoneNumber(user.getPhoneNumber())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * UserDto를 User 엔티티로 변환합니다.
     * 비밀번호 필드는 포함되지 않으므로 새로운 사용자 생성에는 사용할 수 없습니다.
     */
    public User toEntity() {
        Company company = Company.builder()
                .name(this.companyName)
                .ceoName(this.ceoName)
                .companyCode(this.companyCode)
                .companyPhoneNumber(this.companyPhoneNumber).build();

        return User.builder()
                .id(this.id)
                .email(this.email)
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .company(company)
                .build();
    }
}