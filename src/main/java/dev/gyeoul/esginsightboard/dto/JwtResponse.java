package dev.gyeoul.esginsightboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private Long userId;
    private String email;
    private String name;
    private String companyName;
    private Date expiryDate;
    
    // 테스트용 토큰 필드
    private String testToken;
} 