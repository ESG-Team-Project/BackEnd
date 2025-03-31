package dev.gyeoul.esginsightboard.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

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
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
} 