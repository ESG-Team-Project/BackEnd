package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.validation.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 정보를 담는 DTO 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청")
@PasswordMatch(
    password = "password",
    checkPassword = "checkPassword",
    message = "비밀번호와 확인용 비밀번호가 일치하지 않습니다"
)
public class SignupRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    @NotBlank(message = "이메일은 필수 입력값입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;
    
    @Schema(description = "비밀번호 (8자 이상, 영문/숫자/특수문자 포함)", example = "Password123!", required = true)
    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;
    
    @Schema(description = "비밀번호 확인", example = "Password123!", required = true)
    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다")
    private String checkPassword;
    
    @Schema(description = "회사명", example = "(주)환경기술", required = true)
    @NotBlank(message = "회사명은 필수 입력값입니다")
    @Size(max = 100, message = "회사명은 100자 이하로 입력해주세요")
    private String companyName;
    
    @Schema(description = "대표자명", example = "김대표", required = true)
    @NotBlank(message = "대표자명은 필수 입력값입니다")
    @Size(max = 50, message = "대표자명은 50자 이하로 입력해주세요")
    private String ceoName;
    
    @Schema(description = "회사 코드 (영문+숫자 3-20자)", example = "ECO2023", required = true)
    @NotBlank(message = "회사 코드는 필수 입력값입니다")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "회사 코드는 영문과 숫자로 이루어진 3-20자리여야 합니다")
    private String companyCode;
    
    @Schema(description = "회사 전화번호", example = "02-1234-5678", required = true)
    @NotBlank(message = "회사 전화번호는 필수 입력값입니다")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다(예: 02-1234-5678)")
    private String companyPhoneNumber;

    @Schema(description = "사용자 이름", example = "홍길동", required = true)
    @NotBlank(message = "이름은 필수 입력값입니다")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요")
    private String name;
    
    @Schema(description = "개인 전화번호", example = "010-1234-5678", required = true)
    @NotBlank(message = "개인 전화번호는 필수 입력값입니다")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다(예: 010-1234-5678)")
    private String phoneNumber;
} 