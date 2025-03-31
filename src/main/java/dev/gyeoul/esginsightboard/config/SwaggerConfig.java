package dev.gyeoul.esginsightboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("ESG Insight Board API")
                .version("v1.0")
                .description("ESG Insight Board API 문서")
                .contact(new Contact()
                        //.name("ESG Insight Board")
                        //.email("contact@example.com")
                        //.url("https://example.com")
                        )
                .license(new License()
                        //.name("Apache 2.0")
                        //.url("http://www.apache.org/licenses/LICENSE-2.0.html")
                        );

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .info(info)
                .addSecurityItem(securityRequirement)
                .tags(Arrays.asList(
                        new Tag().name("사용자 관리 API").description("회원가입, 로그인, 테스트 토큰 발급 등 사용자 관리 API"),
                        new Tag().name("GRI 데이터 항목").description("GRI 프레임워크 기반 ESG 데이터 항목 관리 API"),
                        new Tag().name("대시보드").description("ESG 대시보드 정보 API"),
                        new Tag().name("Data Import API").description("CSV 파일을 통한 ESG 데이터 임포트 및 관련 기능")
                ));
    }
} 