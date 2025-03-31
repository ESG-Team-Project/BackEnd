package dev.gyeoul.esginsightboard.util;

import dev.gyeoul.esginsightboard.config.JwtConfig;
import dev.gyeoul.esginsightboard.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtConfig jwtConfig;

    /**
     * 사용자 정보에서 토큰 생성
     * 
     * @param userDto 사용자 정보
     * @return JWT 토큰
     */
    public String generateToken(UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDto.getId());
        claims.put("name", userDto.getName());
        claims.put("email", userDto.getEmail());
        claims.put("companyName", userDto.getCompanyName());
        
        return createToken(claims, userDto.getEmail());
    }
    
    /**
     * 테스트용 토큰 생성
     * 
     * @param email 테스트용 이메일
     * @return JWT 토큰
     */
    public String generateTestToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 999L);
        claims.put("name", "테스트 사용자");
        claims.put("email", email);
        claims.put("companyName", "테스트 회사");
        
        log.info("테스트 토큰 생성: {}", email);
        return createToken(claims, email);
    }

    /**
     * 토큰 생성
     * 
     * @param claims 토큰에 포함될 정보
     * @param subject 토큰의 주체(이메일)
     * @return JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtConfig.secretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰에서 사용자 이메일 추출
     * 
     * @param token 토큰
     * @return 사용자 이메일
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 토큰에서 클레임 추출
     * 
     * @param token 토큰
     * @param claimsResolver 클레임 리졸버 함수
     * @return 클레임에서 추출한 값
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰에서 모든 클레임 추출
     * 
     * @param token 토큰
     * @return 모든 클레임
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 유효성 검사
     * 
     * @param token 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtConfig.secretKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료일자 확인
     * 
     * @param token 토큰
     * @return 만료일자
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 토큰 만료 여부 확인
     * 
     * @param token 토큰
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
} 