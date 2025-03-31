package dev.gyeoul.esginsightboard.dto;

import dev.gyeoul.esginsightboard.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String department;
    private String position;
    private String companyName;
    private String ceoName;
    private String companyCode;
    private String companyPhoneNumber;
    private String phoneNumber;
    private Long companyId;
    private LocalDateTime createdAt;
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
                .department(user.getDepartment())
                .position(user.getPosition())
                .companyName(user.getCompanyName())
                .ceoName(user.getCeoName())
                .companyCode(user.getCompanyCode())
                .companyPhoneNumber(user.getCompanyPhoneNumber())
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
        return User.builder()
                .id(this.id)
                .email(this.email)
                .name(this.name)
                .department(this.department)
                .position(this.position)
                .companyName(this.companyName)
                .ceoName(this.ceoName)
                .companyCode(this.companyCode)
                .companyPhoneNumber(this.companyPhoneNumber)
                .phoneNumber(this.phoneNumber)
                .build();
    }
} 