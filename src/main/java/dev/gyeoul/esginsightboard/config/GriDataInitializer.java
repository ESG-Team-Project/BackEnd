package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.service.GriDataItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class GriDataInitializer {

    private final GriDataItemService griDataItemService;

    @Bean
    @Profile("!test") // 테스트 환경에서는 실행되지 않도록 설정
    public CommandLineRunner initGriData() {
        return args -> {
            // 이미 데이터가 있는지 확인
            List<GriDataItemDto> existingData = griDataItemService.getAllGriDataItems();
            if (!existingData.isEmpty()) {
                System.out.println("이미 GRI 데이터가 존재합니다. 초기화를 건너뜁니다.");
                return;
            }

            // 현재 날짜 및 보고 기간 설정
            LocalDate now = LocalDate.now();
            LocalDate periodStart = LocalDate.of(now.getYear() - 1, 1, 1);
            LocalDate periodEnd = LocalDate.of(now.getYear() - 1, 12, 31);

            // 샘플 데이터 생성 - Environmental 카테고리
            List<GriDataItemDto> environmentalData = Arrays.asList(
                    // GRI 302: 에너지
                    GriDataItemDto.builder()
                            .standardCode("GRI 302")
                            .disclosureCode("302-1")
                            .disclosureTitle("조직 내 에너지 소비")
                            .disclosureValue("전기: 15,000 MWh, 가스: 5,000 MWh, 총 20,000 MWh 사용")
                            .description("회사 내 모든 사업장에서 소비한 에너지의 총량")
                            .numericValue(20000.0)
                            .unit("MWh")
                            .category("Environmental")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증완료")
                            .verificationProvider("한국환경인증원")
                            .build(),
                    
                    // GRI 303: 용수 및 배출
                    GriDataItemDto.builder()
                            .standardCode("GRI 303")
                            .disclosureCode("303-3")
                            .disclosureTitle("용수 취수")
                            .disclosureValue("총 500,000 m³ 취수, 재활용수 150,000 m³ 사용")
                            .description("기업이 취수한 용수의 양과 수원")
                            .numericValue(500000.0)
                            .unit("m³")
                            .category("Environmental")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증완료")
                            .verificationProvider("한국환경인증원")
                            .build(),
                    
                    // GRI 305: 배출
                    GriDataItemDto.builder()
                            .standardCode("GRI 305")
                            .disclosureCode("305-1")
                            .disclosureTitle("직접 온실가스 배출량(Scope 1)")
                            .disclosureValue("10,000 tCO2eq")
                            .description("회사가 소유하거나 통제하는 배출원에서 발생하는 온실가스 직접 배출량")
                            .numericValue(10000.0)
                            .unit("tCO2eq")
                            .category("Environmental")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증완료")
                            .verificationProvider("한국환경인증원")
                            .build()
            );

            // 샘플 데이터 생성 - Social 카테고리
            List<GriDataItemDto> socialData = Arrays.asList(
                    // GRI 401: 고용
                    GriDataItemDto.builder()
                            .standardCode("GRI 401")
                            .disclosureCode("401-1")
                            .disclosureTitle("신규 채용과 이직")
                            .disclosureValue("신규 채용: 150명, 이직: 50명, 이직률: 5%")
                            .description("보고 기간 동안의 신규 채용 인원 수와 이직 인원 수")
                            .numericValue(5.0)
                            .unit("%")
                            .category("Social")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증중")
                            .verificationProvider("한국ESG인증원")
                            .build(),
                    
                    // GRI 403: 산업안전보건
                    GriDataItemDto.builder()
                            .standardCode("GRI 403")
                            .disclosureCode("403-9")
                            .disclosureTitle("업무 관련 상해")
                            .disclosureValue("부상 건수: 5건, 사망 건수: 0건, 근로손실일수율: 0.05")
                            .description("업무 관련 상해의 유형과 발생률")
                            .numericValue(0.05)
                            .unit("LTIFR")
                            .category("Social")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증중")
                            .verificationProvider("한국ESG인증원")
                            .build(),
                    
                    // GRI 405: 다양성과 기회균등
                    GriDataItemDto.builder()
                            .standardCode("GRI 405")
                            .disclosureCode("405-1")
                            .disclosureTitle("거버넌스 기구 및 임직원 다양성")
                            .disclosureValue("여성 임원 비율: 20%, 장애인 고용 비율: 3.5%, 외국인 고용 비율: 5%")
                            .description("경영진과 직원의 다양성 지표")
                            .numericValue(20.0)
                            .unit("%")
                            .category("Social")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("검증중")
                            .verificationProvider("한국ESG인증원")
                            .build()
            );

            // 샘플 데이터 생성 - Governance 카테고리
            List<GriDataItemDto> governanceData = Arrays.asList(
                    // GRI 205: 반부패
                    GriDataItemDto.builder()
                            .standardCode("GRI 205")
                            .disclosureCode("205-2")
                            .disclosureTitle("반부패 정책 및 절차에 관한 공지와 훈련")
                            .disclosureValue("반부패 교육 이수율: 100%, 내부고발제도 운영")
                            .description("조직의 반부패 정책과 절차에 대한 커뮤니케이션 및 교육 현황")
                            .numericValue(100.0)
                            .unit("%")
                            .category("Governance")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("미검증")
                            .verificationProvider("")
                            .build(),
                    
                    // GRI 206: 경쟁저해행위
                    GriDataItemDto.builder()
                            .standardCode("GRI 206")
                            .disclosureCode("206-1")
                            .disclosureTitle("경쟁저해행위, 독과점 등 불공정한 거래행위에 대한 법적 조치")
                            .disclosureValue("불공정 거래 관련 법적 조치 수: 0건")
                            .description("보고 기간 동안 경쟁저해행위 및 독점금지법 위반으로 인한 법적 조치의 수와 그 결과")
                            .numericValue(0.0)
                            .unit("건")
                            .category("Governance")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("미검증")
                            .verificationProvider("")
                            .build(),
                    
                    // GRI 207: 조세
                    GriDataItemDto.builder()
                            .standardCode("GRI 207")
                            .disclosureCode("207-1")
                            .disclosureTitle("조세 접근법")
                            .disclosureValue("투명한 세금 납부 및 보고를 위한 조세 전략 수립 및 이행")
                            .description("조직의 세금 전략과 접근 방식")
                            .numericValue(null)
                            .unit("")
                            .category("Governance")
                            .reportingPeriodStart(periodStart)
                            .reportingPeriodEnd(periodEnd)
                            .verificationStatus("미검증")
                            .verificationProvider("")
                            .build()
            );

            // 모든 데이터 저장
            List<GriDataItemDto> allData = new java.util.ArrayList<>();
            allData.addAll(environmentalData);
            allData.addAll(socialData);
            allData.addAll(governanceData);

            // 데이터 저장
            allData.forEach(data -> {
                griDataItemService.saveGriDataItem(data);
                System.out.println(data.getDisclosureCode() + " (" + data.getDisclosureTitle() + ") 데이터가 저장되었습니다.");
            });

            System.out.println("GRI 초기 데이터 로딩이 완료되었습니다.");
        };
    }
} 