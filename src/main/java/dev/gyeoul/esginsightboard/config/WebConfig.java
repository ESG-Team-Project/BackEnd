package dev.gyeoul.esginsightboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * 웹 MVC 구성
 * <p>
 * 정적 리소스, CORS 및 기타 웹 관련 설정을 구성합니다.
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 설정
     * <p>
     * 모든 오리진에서의 API 접근을 허용하고, 인증 헤더 및 컨텐츠 타입 헤더를 포함합니다.
     * </p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Expires")
                .maxAge(3600)
                .exposedHeaders("Content-Disposition", "Content-Length", "Content-Type", "Expires");
    }

    /**
     * 정적 리소스 설정
     * <p>
     * 문서, 이미지 등의 정적 리소스에 대한 경로 및 캐싱 설정을 구성합니다.
     * </p>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 리소스
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        
        // 정적 문서 리소스
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("classpath:/static/documents/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
                
        // 기타 정적 리소스
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
                
        // 에러 페이지용 리소스
        registry.addResourceHandler("/error/**")
                .addResourceLocations("classpath:/static/error/")
                .setCacheControl(CacheControl.noCache());
    }
} 