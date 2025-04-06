package dev.gyeoul.esginsightboard.config;

import dev.gyeoul.esginsightboard.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 문서 파일 초기화 설정
 * <p>
 * 애플리케이션 시작 시 필요한 샘플 문서 파일을 생성합니다.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DocumentInitializer {

    private final DocumentService documentService;
    private final ResourceLoader resourceLoader;

    /**
     * 애플리케이션 시작 시 샘플 문서 파일을 생성합니다.
     */
    @Bean
    public CommandLineRunner initializeDocuments() {
        return args -> {
            log.info("샘플 문서 파일 초기화 시작...");
            
            try {
                // 문서 디렉토리 생성
                Path documentsDir = Paths.get("src/main/resources/static/documents");
                if (!Files.exists(documentsDir)) {
                    Files.createDirectories(documentsDir);
                    log.info("문서 디렉토리 생성: {}", documentsDir);
                }
                
                // GRI 프레임워크 문서 생성
                generateFrameworkDocument("gri", "pdf");
                generateFrameworkDocument("gri", "docx");
                
                log.info("샘플 문서 파일 초기화 완료");
            } catch (Exception e) {
                log.error("샘플 문서 파일 초기화 중 오류 발생: {}", e.getMessage());
            }
        };
    }
    
    /**
     * 프레임워크 문서를 생성합니다.
     * 
     * @param frameworkId 프레임워크 ID
     * @param format 문서 형식
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    private void generateFrameworkDocument(String frameworkId, String format) throws IOException {
        String filename = String.format("%s_framework.%s", frameworkId, format);
        Path filePath = Paths.get("src/main/resources/static/documents", filename);
        
        if (!Files.exists(filePath)) {
            log.info("{}_{} 프레임워크 문서 생성 중...", frameworkId.toUpperCase(), format.toUpperCase());
            
            byte[] documentBytes = documentService.getFrameworkDocument(frameworkId, format);
            Files.write(filePath, documentBytes);
            
            log.info("프레임워크 문서 생성 완료: {}", filePath);
        } else {
            log.info("프레임워크 문서가 이미 존재합니다: {}", filePath);
        }
    }
} 