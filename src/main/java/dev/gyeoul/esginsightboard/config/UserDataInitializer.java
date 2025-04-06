package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.entity.TimeSeriesDataPoint;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.repository.GriDataItemRepository;
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
    private final GriDataItemRepository griDataItemRepository;
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
                
                // 샘플 GRI 데이터 생성
                // 시계열 데이터 샘플 - 온실가스 배출량 (Scope 1)
                GriDataItem scope1Emissions = GriDataItem.builder()
                        .standardCode("GRI 305")
                        .disclosureCode("305-1")
                        .disclosureTitle("직접 온실가스 배출량 (Scope 1)")
                        .disclosureValue("시계열 데이터 참조")
                        .description("직접적인 온실가스 배출량(Scope 1)의 연도별 데이터입니다.")
                        .category("E")
                        .company(company)
                        .dataType(GriDataItem.DataType.TIMESERIES)
                        .build();
                
                griDataItemRepository.save(scope1Emissions);
                
                // 시계열 데이터 포인트 추가
                TimeSeriesDataPoint point2020 = TimeSeriesDataPoint.builder()
                        .year(2020)
                        .value("15000")
                        .unit("tCO2eq")
                        .notes("2020년 배출량")
                        .build();
                scope1Emissions.addTimeSeriesDataPoint(point2020);
                
                TimeSeriesDataPoint point2021 = TimeSeriesDataPoint.builder()
                        .year(2021)
                        .value("14500")
                        .unit("tCO2eq")
                        .notes("2021년 배출량")
                        .build();
                scope1Emissions.addTimeSeriesDataPoint(point2021);
                
                TimeSeriesDataPoint point2022 = TimeSeriesDataPoint.builder()
                        .year(2022)
                        .value("13800")
                        .unit("tCO2eq")
                        .notes("2022년 배출량")
                        .build();
                scope1Emissions.addTimeSeriesDataPoint(point2022);
                
                TimeSeriesDataPoint point2023 = TimeSeriesDataPoint.builder()
                        .year(2023)
                        .value("12900")
                        .unit("tCO2eq")
                        .notes("2023년 배출량")
                        .build();
                scope1Emissions.addTimeSeriesDataPoint(point2023);
                
                griDataItemRepository.save(scope1Emissions);
                
                // 텍스트 데이터 샘플 - 기후변화 정책
                GriDataItem climatePolicy = GriDataItem.builder()
                        .standardCode("GRI 305")
                        .disclosureCode("305-5")
                        .disclosureTitle("온실가스 배출량 감축")
                        .disclosureValue("당사는 2030년까지 온실가스 배출량을 2020년 대비 50% 감축하는 목표를 설정하였습니다.")
                        .description("기후변화 대응을 위한 온실가스 감축 정책 및 목표")
                        .category("E")
                        .company(company)
                        .dataType(GriDataItem.DataType.TEXT)
                        .build();
                
                griDataItemRepository.save(climatePolicy);
                
                // 숫자 데이터 샘플 - 에너지 사용량
                GriDataItem energyConsumption = GriDataItem.builder()
                        .standardCode("GRI 302")
                        .disclosureCode("302-1")
                        .disclosureTitle("조직 내 에너지 소비")
                        .disclosureValue("조직 내 총 에너지 소비량")
                        .numericValue(120000.0)
                        .unit("MWh")
                        .description("2023년 기준 조직 내 총 에너지 소비량")
                        .category("E")
                        .company(company)
                        .dataType(GriDataItem.DataType.NUMERIC)
                        .build();
                
                griDataItemRepository.save(energyConsumption);
                
                log.info("기본 사용자 계정 및 샘플 GRI 데이터가 생성되었습니다.");
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