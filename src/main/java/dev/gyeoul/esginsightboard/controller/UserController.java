package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.*;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관리 관련 API 컨트롤러
 * 회원가입, 로그인, 사용자 정보 조회 등의 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리 API", description = "회원가입, 로그인, 테스트 토큰 발급 등 사용자 관리 API")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 회원가입 API
     * 
     * @param request 회원가입 요청 데이터
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입", 
        description = "사용자 정보를 입력받아 회원가입을 진행합니다. 이메일 중복 시 409 에러가 발생합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공", 
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignupRequest request) {
        UserDto userDto = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);     // HTTP 201 Created 상태코드로 생성된 사용자 정보 반환
    }

    /**
     * 로그인 API
     * 
     * @param request 로그인 요청 데이터
     * @return 로그인 응답 (JWT 토큰 및 사용자 정보 포함)
     */
    @PostMapping("/login")
    @Operation(
        summary = "로그인", 
        description = "이메일과 비밀번호를 입력받아 로그인을 진행하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않음")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);        // 서비스 계층에 로그인 요청을 위임하여 처리
        return ResponseEntity.ok(response);     // 로그인 성공 시 200 OK + JWT 포함된 응답 반환
    }

    /**
     * 현재 로그인한 사용자의 정보를 조회하는 API
     * 
     * @param request HTTP 요청 객체 (JWT 토큰 검증 후 사용자 정보가 설정됨)
     * @return 사용자 정보
     */
    @GetMapping("/me")
    @Operation(
        summary = "내 정보 조회", 
        description = "현재 로그인된 사용자의 정보를 조회합니다. JWT 토큰이 필요합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공", 
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 없음")
    })
    public ResponseEntity<UserDto> getMyInfo(HttpServletRequest request) {
        // JWT 인증 필터에서 사용자 정보를 request attribute로 설정했다고 가정
        UserDto user = (UserDto) request.getAttribute("user");

        // 사용자 정보가 존재하면 200 ok, 없으면 401 Unauthorized 반환
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * API 테스트를 위한 테스트 토큰 발급 API
     * 
     * @param email 테스트 토큰에 사용할 이메일
     * @return 테스트 토큰 정보
     */
    @GetMapping("/test-token")
    @Operation(
        summary = "테스트 토큰 발급", 
        description = "Swagger에서 보호된 API를 테스트하기 위한 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "테스트 토큰 발급 성공", 
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> generateTestToken(
            @Parameter(description = "테스트 토큰에 사용할 이메일", example = "test@example.com") 
            @RequestParam(defaultValue = "test@example.com") String email) {

        String token = jwtTokenUtil.generateTestToken(email);                   // 1. 테스트용 JWT 토큰 생성
        Date expiryDate = jwtTokenUtil.getExpirationDateFromToken(token);       // 2. 만료일 추출

        Map<String, Object> response = createTestTokenResponse(token, email, expiryDate);       // 3. 응답 데이터 구성
        log.info("테스트 토큰 발급: {}, 만료일: {}", email, expiryDate);                          // 4. 로그 기록
        
        return ResponseEntity.ok(response);         // 5. 응답 반환
    }
    
    /**
     * 테스트 토큰 응답 데이터를 생성하는 메서드
     */
    private Map<String, Object> createTestTokenResponse(String token, String email, Date expiryDate) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> tokenInfo = new HashMap<>();

        // JWT 토큰 정보 구성
        tokenInfo.put("token", token);                  // JWT 문자열
        tokenInfo.put("type", "Bearer");                // 토큰 타입 명시 (OAuth2 등에서 사용)
        tokenInfo.put("expiryDate", expiryDate);        // 만료일 정보

        // 최종 응답 객체 구성
        response.put("success", true);                          // 처리 성공 여부
        response.put("message", "테스트 토큰이 발급되었습니다");
        response.put("tokenInfo", tokenInfo);                   // 위에서 구성한 토큰 정보 Map
        
        // Swagger UI 사용법
        response.put("swaggerUsage", "Swagger UI 우측 상단의 'Authorize' 버튼을 클릭하고, 발급된 토큰을 입력하세요. (Bearer 접두사 없이)");
        // cURL 명령 예시 포함
        response.put("curlExample", "curl -X GET 'http://localhost:8080/api/users/me' -H 'Authorization: Bearer " + token + "'");
        
        return response;
    }
    
    /**
     * JWT 토큰 테스트 API
     * 
     * @param request HTTP 요청 객체 (JWT 토큰 검증 결과 포함)
     * @return 토큰 검증 결과 및 사용자 정보
     */
    @GetMapping("/verify")
    @Operation(
        summary = "토큰 검증", 
        description = "JWT 토큰의 유효성을 검증하고 사용자 정보를 반환합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> tokenTest(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Authorization 헤더 확인
        String authHeader = request.getHeader("Authorization");
        response.put("authorizationHeader", authHeader);
        
        // 요청에 설정된 사용자 정보 확인
        UserDto user = (UserDto) request.getAttribute("user");
        response.put("authenticated", user != null);
        
        // 사용자 정보가 존재하면 세부 정보 반환
        if (user != null) {
            response.put("userInfo", extractUserInfo(user));
        }
        
        // 응답 반환
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 회원정보 저장 API
     *
     * @param request 사용자가 보낸 회원정보 수정 요청 (이름, 이메일, 비밀번호, 전화번호)
     * @param req HTTP 요청 객체 (JWT 토큰 검증 결과 포함)
     * @return 수정 성공 시 요청 내용을 그대로 반환 (HTTP 200), 인증 실패 시 401 반환
     */
    @PutMapping("/update")
    @Operation(
            summary = "사용자 정보 수정",
            description = "마이페이지에서 사용자 정보를 수정합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserUpdateRequest> updateUser(
            @RequestBody @Valid UserUpdateRequest request, HttpServletRequest req) {
        // JWT 필터를 통해 주입된 현재 사용자 정보 꺼내기
        UserDto currentUser = (UserDto) req.getAttribute("user");

        // 인증되지 않은 경우 401 반환
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 서비스 계층에 사용자 ID와 수정 요청을 넘겨 DB 반영 수행
        userService.updateUser(currentUser.getId(), request);

        // 성공적으로 수정된 경우, 원래 요청 객체를 그대로 반환
        return ResponseEntity.ok(request);
    }

    // 회사 정보 수정
    @PutMapping("/update-company")
    @Operation(
            summary = "회사 정보 수정",
            description = "마이페이지에서 회사 정보 수정"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CompanyUpdateRequest> updateCompany(
            @RequestBody CompanyUpdateRequest request,
            HttpServletRequest req
    ) {
        UserDto currentUser = (UserDto) req.getAttribute("user");

        if(currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updateCompany(currentUser.getCompanyId(), request);
        return ResponseEntity.ok(request);
    }


    /**
     * 사용자 정보에서 필요한 정보만 추출하는 메서드
     */
    private Map<String, Object> extractUserInfo(UserDto user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("companyName", user.getCompanyName());
        return userInfo;
    }
} 