package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.entity.EsgCategory;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.EsgIndicator;
import dev.gyeoul.esginsightboard.repository.EsgCategoryRepository;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.repository.EsgIndicatorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EsgCategoryRepository esgCategoryRepository;
    private final CompanyRepository companyRepository;
    private final EsgIndicatorRepository esgIndicatorRepository;

    public DataInitializer(
            EsgCategoryRepository esgCategoryRepository,
            CompanyRepository companyRepository,
            EsgIndicatorRepository esgIndicatorRepository
    ) {
        this.esgCategoryRepository = esgCategoryRepository;
        this.companyRepository = companyRepository;
        this.esgIndicatorRepository = esgIndicatorRepository;
    }

    @Override
    public void run(String... args) {
        // 중복 방지를 위해 초기화는 한 번만 수행
        if (esgCategoryRepository.count() == 0 && companyRepository.count() == 0 && esgIndicatorRepository.count() == 0) {
            // ESG 카테고리
            EsgCategory eCategory = esgCategoryRepository.save(EsgCategory.builder().category("E").build());
            EsgCategory sCategory = esgCategoryRepository.save(EsgCategory.builder().category("S").build());
            EsgCategory gCategory = esgCategoryRepository.save(EsgCategory.builder().category("G").build());

            // 테스트 회사
            Company company = Company.builder()
                    .id(1L) // 직접 ID 지정할 경우 @GeneratedValue 없어야 함
                    .name("테스트기업")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            companyRepository.save(company);

            // ESG 지표
            EsgIndicator indicator1 = EsgIndicator.builder()
                    .indicatorCode("202-1")
                    .indicatorTitle("직접적인 경제가치 발생과 분배")
                    .category(eCategory)
                    .build();
            esgIndicatorRepository.save(indicator1);


            EsgIndicator indicator2 = EsgIndicator.builder()
                    .indicatorCode("301-1")
                    .indicatorTitle("기반시설 투자와 지원")
                    .category(sCategory)
                    .build();
            esgIndicatorRepository.save(indicator2);
        }
    }
}
