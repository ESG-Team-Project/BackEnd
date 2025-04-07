package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.*;
import dev.gyeoul.esginsightboard.service.UserService;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 * 회원가입, 로그인 등의 인증 관련 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "회원가입, 로그인 등 인증 관련 API")
public class AuthController {

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
        log.info("회원가입 요청: {}", request.getEmail());
        UserDto userDto = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
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
        log.info("로그인 요청: {}", request.getEmail());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 검증 API
     * 
     * @param request 토큰 검증 요청
     * @return 토큰 유효성 결과
     */
    @PostMapping("/verify")
    @Operation(
        summary = "토큰 검증", 
        description = "JWT 토큰의 유효성을 검증합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검증 성공", 
            content = @Content(schema = @Schema(implementation = TokenVerificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 형식의 토큰")
    })
    public ResponseEntity<TokenVerificationResponse> verifyToken(@RequestBody TokenVerificationRequest request) {
        String token = request.getToken();
        
        boolean isValid = jwtTokenUtil.validateToken(token);
        String email = null;
        
        if (isValid) {
            email = jwtTokenUtil.getEmailFromToken(token);
        }
        
        TokenVerificationResponse response = TokenVerificationResponse.builder()
            .valid(isValid)
            .username(email)
            .build();
            
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이메일 중복 체크 API
     * 
     * @param request 이메일 체크 요청
     * @return 이메일 사용 가능 여부
     */
    @PostMapping("/check-email")
    @Operation(
        summary = "이메일 중복 체크", 
        description = "회원가입 시 입력한 이메일의 사용 가능 여부를 확인합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 체크 성공", 
            content = @Content(schema = @Schema(implementation = EmailCheckResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 형식")
    })
    public ResponseEntity<EmailCheckResponse> checkEmail(@Valid @RequestBody EmailCheckRequest request) {
        log.info("이메일 중복 체크 요청: {}", request.getEmail());
        
        boolean isAvailable = userService.isEmailAvailable(request.getEmail());
        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        
        EmailCheckResponse response = EmailCheckResponse.builder()
            .available(isAvailable)
            .message(message)
            .build();
            
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이메일 중복 체크 API (GET 메소드)
     * 
     * @param email 확인할 이메일
     * @return 이메일 사용 가능 여부
     */
    @GetMapping("/check-email")
    @Operation(
        summary = "이메일 중복 체크 (GET)", 
        description = "회원가입 시 입력한 이메일의 사용 가능 여부를 확인합니다 (URL 파라미터 사용)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 체크 성공", 
            content = @Content(schema = @Schema(implementation = EmailCheckResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 형식")
    })
    public ResponseEntity<EmailCheckResponse> checkEmailByGet(
            @RequestParam @Email(message = "유효한 이메일 형식이 아닙니다") String email) {
        log.info("이메일 중복 체크 요청 (GET): {}", email);
        
        boolean isAvailable = userService.isEmailAvailable(email);
        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        
        EmailCheckResponse response = EmailCheckResponse.builder()
            .available(isAvailable)
            .message(message)
            .build();
            
        return ResponseEntity.ok(response);
    }
} 