package dev.gyeoul.esginsightboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Swagger(OpenAPI) 설정 클래스
 * <p>
 * API 문서화를 위한 Swagger/OpenAPI 3.0 설정을 정의합니다.
 * 각 API 그룹에 대한 태그, 인증 방식, 응답 형식 등을 설정합니다.
 * </p>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("ESG Insight Board API")
                .version("v1.0")
                .description("ESG 데이터 관리 및 분석을 위한 ESG Insight Board API 문서입니다.\n\n" +
                        "이 API는 다음 기능을 제공합니다:\n" +
                        "* 사용자 인증 및 관리\n" +
                        "* ESG 데이터 관리 및 분석\n" +
                        "* ESG 보고서 생성 및 다운로드\n" +
                        "* 프레임워크 문서 제공")
                .contact(new Contact()
                        .name("ESG Insight Board 개발팀")
                        .email("contact@esginsight.example.com")
                        .url("https://esginsight.example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT 인증 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // 서버 정보 추가
        Server localServer = new Server()
                .url("/")
                .description("로컬 개발 서버");

        // 공통 응답 정의
        Map<String, ApiResponse> responses = new HashMap<>();
        
        // 401 Unauthorized 응답
        ApiResponse unauthorizedResponse = new ApiResponse()
                .description("인증되지 않음 - 유효한 인증 토큰이 필요합니다.")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().schema(new Schema<>().type("object"))));
        responses.put("UnauthorizedError", unauthorizedResponse);
        
        // 400 Bad Request 응답
        ApiResponse badRequestResponse = new ApiResponse()
                .description("잘못된 요청 - 요청 형식이 잘못되었거나 필수 파라미터가 누락되었습니다.")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().schema(new Schema<>().type("object"))));
        responses.put("BadRequestError", badRequestResponse);
        
        // 404 Not Found 응답
        ApiResponse notFoundResponse = new ApiResponse()
                .description("리소스를 찾을 수 없음 - 요청한 리소스가 존재하지 않습니다.")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().schema(new Schema<>().type("object"))));
        responses.put("NotFoundError", notFoundResponse);

        // 500 Internal Server Error 응답
        ApiResponse serverErrorResponse = new ApiResponse()
                .description("서버 내부 오류 - 서버에서 처리 중 오류가 발생했습니다.")
                .content(new Content().addMediaType("application/json", 
                        new MediaType().schema(new Schema<>().type("object"))));
        responses.put("ServerError", serverErrorResponse);

        // 외부 문서 정보
        ExternalDocumentation externalDocs = new ExternalDocumentation()
                .description("ESG Insight Board API 사용 가이드")
                .url("https://esginsight.example.com/docs");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme)
                        .addResponses("UnauthorizedError", unauthorizedResponse)
                        .addResponses("BadRequestError", badRequestResponse)
                        .addResponses("NotFoundError", notFoundResponse)
                        .addResponses("ServerError", serverErrorResponse))
                .info(info)
                .externalDocs(externalDocs)
                .addSecurityItem(securityRequirement)
                .servers(Arrays.asList(localServer))
                .tags(Arrays.asList(
                        new Tag().name("사용자 관리 API").description("회원가입, 로그인, 사용자 정보 관리, JWT 토큰 관련 API"),
                        new Tag().name("GRI 데이터 항목").description("GRI 프레임워크 기반 ESG 데이터 항목 관리 API"),
                        new Tag().name("대시보드").description("ESG 데이터 분석 및 대시보드 정보 API"),
                        new Tag().name("Data Import API").description("CSV 파일을 통한 ESG 데이터 임포트 및 관련 기능"),
                        new Tag().name("문서 및 보고서").description("프레임워크 문서 및 회사별 ESG 보고서 다운로드 API"),
                        new Tag().name("회사 관리").description("회사 정보 관리 및 ESG 데이터 분석 API")
                ));
    }
} 