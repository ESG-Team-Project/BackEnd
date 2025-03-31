package dev.gyeoul.esginsightboard.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKeyString;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.header:Authorization}")
    private String header;
    
    @Value("${jwt.prefix:Bearer }")
    private String prefix;
    
    /**
     * JWT 시크릿 키를 반환합니다.
     * 
     * @return JWT 시크릿 키
     */
    public SecretKey secretKey() {
        if (secretKeyString != null && !secretKeyString.isEmpty()) {
            try {
                // Base64로 인코딩된 문자열인 경우 디코딩
                byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (IllegalArgumentException e) {
                // Base64가 아닌 경우, 문자열을 바이트로 변환하여 사용
                byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
                // 키 크기가 부족한 경우 패딩
                if (keyBytes.length < 64) {
                    byte[] paddedKey = new byte[64];
                    System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                    keyBytes = paddedKey;
                }
                return Keys.hmacShaKeyFor(keyBytes);
            }
        } else {
            // 환경 변수가 없는 경우, 안전한 랜덤 키 생성
            return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        }
    }
    
} 