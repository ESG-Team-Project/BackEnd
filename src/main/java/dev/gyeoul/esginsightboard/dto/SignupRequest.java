package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.validation.PasswordMatch;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch(
    password = "password",
    checkPassword = "checkPassword",
    message = "비밀번호와 확인용 비밀번호가 일치하지 않습니다"
)
public class SignupRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;
    
    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다")
    private String checkPassword;
    
    @NotBlank(message = "회사명은 필수 입력값입니다")
    @Size(max = 100, message = "회사명은 100자 이하로 입력해주세요")
    private String companyName;
    
    @NotBlank(message = "대표자명은 필수 입력값입니다")
    @Size(max = 50, message = "대표자명은 50자 이하로 입력해주세요")
    private String ceoName;
    
    @NotBlank(message = "회사 코드는 필수 입력값입니다")
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "회사 코드는 영문과 숫자로 이루어진 3-20자리여야 합니다")
    private String companyCode;
    
    @NotBlank(message = "회사 전화번호는 필수 입력값입니다")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다(예: 02-1234-5678)")
    private String companyPhoneNumber;

    @NotBlank(message = "이름은 필수 입력값입니다")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요")
    private String name;
    
    @NotBlank(message = "개인 전화번호는 필수 입력값입니다")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다(예: 010-1234-5678)")
    private String phoneNumber;
} 