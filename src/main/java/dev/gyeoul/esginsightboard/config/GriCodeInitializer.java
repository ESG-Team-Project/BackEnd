package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.service.GriDataItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GriCodeInitializer {

    private final GriDataItemService griDataItemService;

    @Bean
    @Profile("!test") // 테스트 환경에서는 실행되지 않도록 설정
    public CommandLineRunner initGriCodes() {
        return args -> {
            // 이미 데이터가 있는지 확인
            List<GriDataItemDto> existingData = griDataItemService.getAllGriDataItems();
            if (!existingData.isEmpty()) {
                log.info("이미 GRI 데이터가 존재합니다. 전체 GRI 코드 초기화를 건너뜁니다.");
                return;
            }

            // 현재 날짜 및 보고 기간 설정
            LocalDate now = LocalDate.now();
            LocalDate periodStart = LocalDate.of(now.getYear() - 1, 1, 1);
            LocalDate periodEnd = LocalDate.of(now.getYear() - 1, 12, 31);

            // 모든 GRI 코드 초기화
            log.info("GRI 코드 초기화를 시작합니다...");
            
            List<GriDataItemDto> allCodes = new ArrayList<>();
            allCodes.addAll(createGri100Codes(periodStart, periodEnd));
            allCodes.addAll(createGri200Codes(periodStart, periodEnd));
            allCodes.addAll(createGri300Codes(periodStart, periodEnd));
            allCodes.addAll(createGri400Codes(periodStart, periodEnd));

            // 데이터 저장
            int savedCount = 0;
            for (GriDataItemDto code : allCodes) {
                griDataItemService.saveGriDataItem(code, 1L); // 기본 회사 ID 1로 설정
                savedCount++;
                
                if (savedCount % 50 == 0) {
                    log.info("GRI 코드 {} / {} 개 저장 완료", savedCount, allCodes.size());
                }
            }

            log.info("GRI 코드 초기화가 완료되었습니다. 총 {}개의 코드가 저장되었습니다.", savedCount);
        };
    }

    /**
     * GRI 100 시리즈 코드 생성 (일반보고 - Universal Standards)
     */
    private List<GriDataItemDto> createGri100Codes(LocalDate periodStart, LocalDate periodEnd) {
        List<GriDataItemDto> codes = new ArrayList<>();
        
        // GRI 102: 일반보고
        addGriCode(codes, "GRI 102", "102-1", "조직 명칭", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-2", "활동 및 대표 브랜드, 제품 및 서비스", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-3", "본사의 위치", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-4", "사업 지역", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-5", "소유 구조 특성 및 법적 형태", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-6", "시장 영역(제품과 서비스가 제공되는 위치, 고객 유형 등)", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-7", "조직의 규모(임직원 수, 사업장 수, 순 매출 등)", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-8", "임직원 및 근로자에 대한 정보", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-9", "조직의 공급망", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-10", "보고기간 동안 발생한 조직 및 공급망의 중대한 변화", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-11", "사전예방 원칙 및 접근", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-12", "조직이 가입하였거나 지지하는 외부 이니셔티브(사회헌장, 원칙 등)", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-13", "협회 맴버십 현황", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-14", "최고 의사 결정권자 성명서", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-15", "주요 영향, 위기 그리고 기회", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-16", "가치, 원칙, 표준, 행동강령", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-17", "윤리 관리 안내 및 고충처리 메커니즘", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-18", "지배구조", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-19", "권한 위임 절차", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-20", "경제적, 환경적, 사회적 토픽에 대한 임원진 책임", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-21", "이해관계자와 경제, 환경, 사회적 토픽 협의", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-22", "최고의사결정기구와 산하 위원회의 구성", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-23", "최고의사결정기구의 의장", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-24", "최고의사결정기구 및 산하위원회의 임명과 선정 절차", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-25", "이해관계 상충", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-26", "목적, 가치 및 전략 수립에 관한 최고의사결정기구의 역할", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-27", "최고의사결정기구의 공동지식 강화 및 개발절차", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-28", "최고의사결정기구의 성과평가에 대한 절차", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-29", "최고의사결정기구의 경제적, 환경적, 사회적 영향 파악과 관리", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-30", "리스크관리 절차의 효과성", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-31", "경제적, 환경적, 사회적 토픽에 대한 점검", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-32", "지속가능성 보고에 대한 최고의사결정기구의 역할", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-33", "중요 사항을 최고의사결정기구에 보고하는 절차", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-34", "중요 사항의 특성 및 보고 횟수", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-35", "보상 정책", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-36", "보수 결정 절차 및 보수자문위원 관여 여부", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-37", "보수에 대한 이해관계자의 참여", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-38", "연찬 총 보상 비율", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-39", "연간 총 보상 인상율", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-40", "조직과 관련 있는 이해관계자 집단 리스트", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-41", "전체 임직원 중 단체협약이 적용되는 임직원의 비율", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-42", "이해관계자 파악 및 선정", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-43", "이해관계자 참여 방식", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-44", "이해관계자 참여를 통해 제기된 핵심 토픽 및 관심사", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-45", "조직의 연결 재무제표에 포함된 자회사 및 합작회사 리스트", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-46", "보고 내용 및 토픽의 경계 정의", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-47", "보고서 내용 결정 과정에서 파악한 모든 중요 토픽 리스트", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-48", "이전 보고서에 기록된 정보 수정", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-49", "중요 토픽 및 주제범위에 대한 변화", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-50", "보고 기간", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-51", "가장 최근 보고서 발간 일자", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-52", "보고 주기(매년, 격년 등)", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-53", "보고서에 대한 문의처", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-54", "GRI Standards에 따른 보고 방식", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-55", "적용한 GRI 인덱스", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 102", "102-56", "보고서 외부 검증", "일반", periodStart, periodEnd);

        // GRI 103: 경영 접근법
        addGriCode(codes, "GRI 103", "103-1", "중대 토픽과 그 경계에 대한 설명", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 103", "103-2", "경영 접근 방식의 목적, 토픽 관리 방법", "일반", periodStart, periodEnd);
        addGriCode(codes, "GRI 103", "103-3", "경영 방식 평가 절차, 결과, 조정사항", "일반", periodStart, periodEnd);
        
        return codes;
    }

    /**
     * GRI 200 시리즈 코드 생성 (경제적 성과 - Economic)
     */
    private List<GriDataItemDto> createGri200Codes(LocalDate periodStart, LocalDate periodEnd) {
        List<GriDataItemDto> codes = new ArrayList<>();
        
        // GRI 201: 경제성과
        addGriCode(codes, "GRI 201", "201-1", "직접적인 경제가치 발생과 분배", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 201", "201-2", "기후변화가 조직의 활동에 미치는 재무적 영향 및 기타 위험과 기회", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 201", "201-3", "조직의 확정급여형 연금제도 채무 충당", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 201", "201-4", "국가별 정부의 재정지원 금액", "Economic", periodStart, periodEnd);
        
        // GRI 202: 시장지위
        addGriCode(codes, "GRI 202", "202-1", "주요 사업장이 위치한 지역의 최저 임금과 비교한 성별 기본초임 임금기준, 최저임금 보상평가 방안 등 공개", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 202", "202-2", "주요 사업장의 현지에서 고용된 고위 경영진 및 정의", "Economic", periodStart, periodEnd);
        
        // GRI 203: 간접 경제효과
        addGriCode(codes, "GRI 203", "203-1", "사회기반시설 투자와 지원 서비스의 개발 및 영향(지역사회 긍정적, 부정적 영향 평가, 기부 등)", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 203", "203-2", "영향 규모 등 중요한 간접 경제효과 영향의 예시(긍정적, 부정적 영향 포함)", "Economic", periodStart, periodEnd);
        
        // GRI 204: 조달 관행
        addGriCode(codes, "GRI 204", "204-1", "주요 사업장에서 현지 공급업체에 지급하는 구매 비율", "Economic", periodStart, periodEnd);
        
        // GRI 205: 반부패
        addGriCode(codes, "GRI 205", "205-1", "부패 위험을 평가한 사업장의 수 및 비율과 파악된 중요한 위험", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 205", "205-2", "반부패 정책 및 절차에 관한 공지와 관련 교육 현황", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 205", "205-3", "확인된 부패 사례와 이에 대한 조치", "Economic", periodStart, periodEnd);
        
        // GRI 206: 반경쟁적 행위
        addGriCode(codes, "GRI 206", "206-1", "경쟁저해행위, 독과점 등 불공정한 거래행위에 대한 법적 조치의 수와 그 결과", "Economic", periodStart, periodEnd);
        
        // GRI 207: 세금
        addGriCode(codes, "GRI 207", "207-1", "세금 관리에 대한 접근법", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 207", "207-2", "세금 관련 거버넌스, 통제 및 리스크 관리", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 207", "207-3", "세금 관련 이해관계자 소통 및 고충 처리 절차", "Economic", periodStart, periodEnd);
        addGriCode(codes, "GRI 207", "207-4", "국가별 세무 내역 공시(연결재무제표가 포괄하는 세무 당국)", "Economic", periodStart, periodEnd);
        
        return codes;
    }

    /**
     * GRI 300 시리즈 코드 생성 (환경적 성과 - Environmental)
     */
    private List<GriDataItemDto> createGri300Codes(LocalDate periodStart, LocalDate periodEnd) {
        List<GriDataItemDto> codes = new ArrayList<>();
        
        // GRI 301: 원재료
        addGriCode(codes, "GRI 301", "301-1", "사용된 원료의 중량과 부피", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 301", "301-2", "재활용 투입재 사용", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 301", "301-3", "판매된 제품 및 그 포장재의 재생 비율", "Environmental", periodStart, periodEnd);
        
        // GRI 302: 에너지
        addGriCode(codes, "GRI 302", "302-1", "조직 내 에너지 소비", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 302", "302-2", "조직 외부 에너지 소비", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 302", "302-3", "에너지 집약도", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 302", "302-4", "에너지 사용량 절감", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 302", "302-5", "제품 및 서비스의 에너지 요구량 감축", "Environmental", periodStart, periodEnd);
        
        // GRI 303: 용수
        addGriCode(codes, "GRI 303", "303-1", "공유 자원으로서의 용수 활용", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 303", "303-2", "방류수 관련 영향 관리", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 303", "303-3", "용수 취수량", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 303", "303-4", "용수 방류량", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 303", "303-5", "용수 사용량", "Environmental", periodStart, periodEnd);
        
        // GRI 304: 생물다양성
        addGriCode(codes, "GRI 304", "304-1", "생태계 보호지역/주변지역에 소유, 임대, 관리하는 사업장", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 304", "304-2", "생태계 보호지역/주변지역에 사업활동, 제품, 서비스 등으로 인한 영향", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 304", "304-3", "서식지 보호 또는 복구", "Environmental", periodStart, periodEnd);
        
        // GRI 305: 배출
        addGriCode(codes, "GRI 305", "305-1", "직접 온실가스 배출량(Scope1)", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 305", "305-2", "간접 온실가스 배출량(Scope2)", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 305", "305-3", "기타 간접 온실가스 배출량(Scope3)", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 305", "305-4", "온실가스 배출 집약도", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 305", "305-5", "온실가스 배출 감축", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 305", "305-6", "오존파괴물질 배출", "Environmental", periodStart, periodEnd);
        
        // GRI 306: 폐수 및 폐기물
        addGriCode(codes, "GRI 306", "306-1", "폐기물 발생 및 폐기물 관련 주요 영향", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 306", "306-2", "폐기물 관련 주요 영향 관리", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 306", "306-3", "폐기물 발생량 및 종류", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 306", "306-4", "폐기물 재활용", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 306", "306-5", "폐기물 매립", "Environmental", periodStart, periodEnd);
        
        // GRI 307: 컴플라이언스
        addGriCode(codes, "GRI 307", "307-1", "환경법 및 규정 위반으로 부과된 중요한 벌금의 액수 및 비금전적 제재조치의 수", "Environmental", periodStart, periodEnd);
        
        // GRI 308: 공급업체 환경 평가
        addGriCode(codes, "GRI 308", "308-1", "환경 기준 심사를 거친 신규 공급업체", "Environmental", periodStart, periodEnd);
        addGriCode(codes, "GRI 308", "308-2", "공급망 내 실질적 또는 잠재적인 중대한 부정적 환경영향 및 이에 대한 조치", "Environmental", periodStart, periodEnd);
        
        return codes;
    }

    /**
     * GRI 400 시리즈 코드 생성 (사회적 성과 - Social)
     */
    private List<GriDataItemDto> createGri400Codes(LocalDate periodStart, LocalDate periodEnd) {
        List<GriDataItemDto> codes = new ArrayList<>();
        
        // GRI 401: 고용
        addGriCode(codes, "GRI 401", "401-1", "신규채용, 퇴직자 수 및 비율", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 401", "401-2", "상근직에게만 제공하는 복리후생", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 401", "401-3", "육아휴직", "Social", periodStart, periodEnd);
        
        // GRI 402: 노사관계
        addGriCode(codes, "GRI 402", "402-1", "경영상 변동에 관한 최소 통지기간", "Social", periodStart, periodEnd);
        
        // GRI 403: 산업안전보건
        addGriCode(codes, "GRI 403", "403-1", "산업안전보건시스템", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-2", "위험 식별, 리스크 평가, 사고 조사", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-3", "산업 보건 지원 프로그램", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-4", "산업안전보건에 대한 근로자 참여 및 커뮤니케이션", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-5", "직업 건강 및 안전에 대한 근로자 교육", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-6", "근로자 건강 증진을 위한 프로그램 설명", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-7", "사업 관계로 인해 직접적인 영향을 미치는 산업보건 및 안전 영향에 대한 예방 및 완화", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-8", "산업안전보건 관리시스템의 적용을 받는 근로자", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-9", "업무 관련 상해", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 403", "403-10", "업무 관련 질병", "Social", periodStart, periodEnd);
        
        // GRI 404: 훈련 및 교육
        addGriCode(codes, "GRI 404", "404-1", "임직원 1인당 평균 교육 시간", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 404", "404-2", "임직원 역량 강화 및 전환 지원을 위한 프로그램", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 404", "404-3", "업무성과 및 경력개발에 대한 정기적인 검토를 받은 근로자 비율", "Social", periodStart, periodEnd);
        
        // GRI 405: 다양성과 기회균등
        addGriCode(codes, "GRI 405", "405-1", "거버넌스 조직 및 임직원 내 다양성", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 405", "405-2", "남성 대비 여성의 기본급 및 보수 비율", "Social", periodStart, periodEnd);
        
        // GRI 406: 차별금지
        addGriCode(codes, "GRI 406", "406-1", "차별 사건 및 이에 대한 시정조치", "Social", periodStart, periodEnd);
        
        // GRI 407: 결사 및 단체교섭의 자유
        addGriCode(codes, "GRI 407", "407-1", "결사 및 단체교섭의 자유 침해 위험이 있는 사업장 및 공급업체", "Social", periodStart, periodEnd);
        
        // GRI 408: 아동노동
        addGriCode(codes, "GRI 408", "408-1", "아동 노동 발생 위험이 높은 사업장 및 협력회사", "Social", periodStart, periodEnd);
        
        // GRI 409: 강제노동
        addGriCode(codes, "GRI 409", "409-1", "강제 노동 발생 위험이 높은 사업장 및 협력회사", "Social", periodStart, periodEnd);
        
        // GRI 410: 보안관행
        addGriCode(codes, "GRI 410", "410-1", "인권 정책 및 절차에 관한 훈련을 받은 보안요원", "Social", periodStart, periodEnd);
        
        // GRI 411: 원주민 권리
        addGriCode(codes, "GRI 411", "411-1", "원주민 권리 침해 사건의 수", "Social", periodStart, periodEnd);
        
        // GRI 412: 인권평가
        addGriCode(codes, "GRI 412", "412-1", "인권검토 또는 인권영향평가 대상인 사업장의 수와 비율", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 412", "412-2", "사업과 관련된 인권 정책 및 절차에 관한 임직원 교육", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 412", "412-3", "인권 조항 또는 인권 심사 시행을 포함한 주요 투자 협약과 계약", "Social", periodStart, periodEnd);
        
        // GRI 413: 지역사회
        addGriCode(codes, "GRI 413", "413-1", "지역사회 참여, 영향 평가 그리고 발전프로그램 운영 비율", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 413", "413-2", "지역사회에 중대한 실질적/잠재적인 부정적 영향이 존재하는 사업장", "Social", periodStart, periodEnd);
        
        // GRI 414: 공급망 관리
        addGriCode(codes, "GRI 414", "414-1", "사회적 영향평가를 통해 스크리닝된 신규 협력회사", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 414", "414-2", "공급망 내 주요한 부정적인 사회 영향과 이에 대한 시행 조치", "Social", periodStart, periodEnd);
        
        // GRI 415: 공공정책
        addGriCode(codes, "GRI 415", "415-1", "국가별, 수령인/수혜자별 기부한 정치자금 총규모", "Social", periodStart, periodEnd);
        
        // GRI 416: 고객 안전보건
        addGriCode(codes, "GRI 416", "416-1", "제품 및 서비스군의 안전보건 영향 평가", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 416", "416-2", "제품 및 서비스의 안전보건 영향에 관한 규정 위반 사건", "Social", periodStart, periodEnd);
        
        // GRI 417: 마케팅 및 라벨링
        addGriCode(codes, "GRI 417", "417-1", "정보 및 라벨을 위해 필요한 제품/서비스 정보 유형", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 417", "417-2", "제품 및 서비스 정보와 라벨링에 관한 법률규정 및 자율규정을 위반한 사건", "Social", periodStart, periodEnd);
        addGriCode(codes, "GRI 417", "417-3", "마케팅 커뮤니케이션과 관련된 규정 위반", "Social", periodStart, periodEnd);
        
        // GRI 418: 고객정보보호
        addGriCode(codes, "GRI 418", "418-1", "고객개인정보보호 위반 및 고객 데이터 분실 관련 제기된 불만 건수", "Social", periodStart, periodEnd);
        
        // GRI 419: 컴플라이언스
        addGriCode(codes, "GRI 419", "419-1", "사회 및 경제 측면의 관련 법규 및 규정 위반에 대한 주요 벌금의 액수", "Social", periodStart, periodEnd);
        
        return codes;
    }

    /**
     * GRI 코드를 추가하는 헬퍼 메서드
     */
    private void addGriCode(List<GriDataItemDto> list, String standardCode, String disclosureCode, String disclosureTitle, String category, LocalDate periodStart, LocalDate periodEnd) {
        GriDataItemDto dto = GriDataItemDto.builder()
                .standardCode(standardCode)
                .disclosureCode(disclosureCode)
                .disclosureTitle(disclosureTitle)
                .category(category)
                .reportingPeriodStart(periodStart)
                .reportingPeriodEnd(periodEnd)
                .verificationStatus("미입력")
                .build();
        
        list.add(dto);
    }
} 