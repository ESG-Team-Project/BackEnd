package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.repository.UserRepository;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initUsers() {
        return args -> {
            // 테스트 계정이 없을 경우에만 생성
            if (!userRepository.existsByEmail("admin@example.com")) {
                Company company = Company.builder()
                        .name("ESG 인사이트")
                        .companyCode("ESG")
                        .companyPhoneNumber("010-1234-5678")
                        .build();
                
                company = companyRepository.save(company);
                
                User admin = User.builder()
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .name("관리자")
                        .company(company)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .enabled(true)
                        .build();
                
                userRepository.save(admin);
                
                User user = User.builder()
                        .email("user@example.com")
                        .password(passwordEncoder.encode("user123"))
                        .name("사용자")
                        .company(company)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .enabled(true)
                        .build();
                
                userRepository.save(user);
                
                log.info("기본 사용자 계정이 생성되었습니다.");
            }
            
            // 테스트 토큰 생성 및 로그 출력
            try {
                String testToken = jwtTokenUtil.generateTestToken("test@example.com");
                log.info("");
                log.info("=============================================");
                log.info("테스트용 JWT 토큰이 생성되었습니다:");
                log.info("Bearer {}", testToken);
                log.info("Swagger에서 Authorization 버튼을 클릭하고 위 토큰을 입력하세요.");
                log.info("=============================================");
                log.info("");
            } catch (Exception e) {
                log.error("테스트 토큰 생성 중 오류 발생: {}", e.getMessage());
            }
        };
    }
} 