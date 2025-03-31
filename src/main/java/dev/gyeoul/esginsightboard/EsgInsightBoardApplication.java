package dev.gyeoul.esginsightboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ESG Insight Board 애플리케이션의 메인 클래스
 * Spring Boot 애플리케이션의 진입점
 * 
 * <p>이 애플리케이션은 기업들이 ESG(환경, 사회, 지배구조) 데이터를 관리하고 
 * 인사이트를 얻을 수 있는 대시보드 서비스를 제공합니다.</p>
 */
@SpringBootApplication
@EnableJpaAuditing  // JPA Auditing 기능 활성화 (생성일시, 수정일시 자동화)
public class EsgInsightBoardApplication {

    /**
     * 애플리케이션의 메인 메서드
     * Spring Boot 애플리케이션을 시작합니다.
     * 
     * @param args 명령줄 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(EsgInsightBoardApplication.class, args);
    }

}
