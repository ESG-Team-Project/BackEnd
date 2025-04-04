package dev.gyeoul.esginsightboard.service;

import dev.gyeoul.esginsightboard.dto.LoginRequest;
import dev.gyeoul.esginsightboard.dto.LoginResponse;
import dev.gyeoul.esginsightboard.dto.SignupRequest;
import dev.gyeoul.esginsightboard.dto.UserDto;
import dev.gyeoul.esginsightboard.entity.Company;
import dev.gyeoul.esginsightboard.entity.User;
import dev.gyeoul.esginsightboard.exception.UserAlreadyExistsException;
import dev.gyeoul.esginsightboard.repository.CompanyRepository;
import dev.gyeoul.esginsightboard.repository.UserRepository;
import dev.gyeoul.esginsightboard.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 * 회원가입, 로그인, 사용자 조회 등의 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 요청 정보
     * @return 가입된 사용자 정보 DTO
     * @throws UserAlreadyExistsException 이미 존재하는 이메일인 경우 발생
     */
    @Transactional
    public UserDto signup(SignupRequest request) {
        // 이메일 중복 확인
        checkEmailDuplication(request.getEmail());
        
        // 비밀번호 일치 확인 (컨트롤러에서 @Valid로 검증하지만 중요한 부분이므로 이중 검증)
        if (!request.getPassword().equals(request.getCheckPassword())) {
            throw new IllegalArgumentException("비밀번호와 확인용 비밀번호가 일치하지 않습니다");
        }

        // 1. 회사 정보 존재 여부 확인 및 저장
        Company company = findOrCreateCompany(request);
        
        // 2. 사용자 엔티티 생성 및 저장 (회사 정보 연결)
        User savedUser = createAndSaveUser(request, company);
        
        log.info("새 사용자 등록 완료: {}, 회사: {}", request.getEmail(), company.getName());
        return UserDto.fromEntity(savedUser);
    }

    /**
     * 회사 정보를 찾거나 생성하는 메서드
     * 
     * @param request 회원가입 요청 정보
     * @return 찾거나 생성된 회사 엔티티
     */
    private Company findOrCreateCompany(SignupRequest request) {
        // 회사 코드로 회사 정보 조회
        Optional<Company> existingCompany = companyRepository.findByBusinessNumber(request.getCompanyCode());
        
        // 존재하면 기존 회사 정보 반환
        if (existingCompany.isPresent()) {
            return existingCompany.get();
        }
        
        // 존재하지 않으면 새로운 회사 정보 생성 및 저장
        Company newCompany = Company.builder()
                .name(request.getCompanyName())
                .businessNumber(request.getCompanyCode())
                .build();
        
        return companyRepository.save(newCompany);
    }

    /**
     * 이메일 중복을 확인하는 메서드
     * 
     * @param email 확인할 이메일
     * @throws UserAlreadyExistsException 이미 존재하는 이메일인 경우 발생
     */
    private void checkEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("이미 등록된 이메일입니다: " + email);
        }
    }

    /**
     * 사용자 엔티티를 생성하고 저장하는 메서드
     * 
     * @param request 회원가입 요청 정보
     * @param company 사용자가 속한 회사 엔티티
     * @return 저장된 사용자 엔티티
     */
    private User createAndSaveUser(SignupRequest request, Company company) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .companyName(request.getCompanyName())
                .ceoName(request.getCeoName())
                .companyCode(request.getCompanyCode())
                .companyPhoneNumber(request.getCompanyPhoneNumber())
                .phoneNumber(request.getPhoneNumber())
                .company(company)  // 회사 엔티티 연결
                .build();

        return userRepository.save(user);
    }

    /**
     * 로그인 처리
     *
     * @param request 로그인 요청 정보
     * @return 로그인 응답 (JWT 토큰과 사용자 정보 포함)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생
     * @throws BadCredentialsException 비밀번호가 일치하지 않는 경우 발생
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = findUserByEmailOrThrow(request.getEmail());
        
        // 비밀번호 검증
        validatePassword(request.getPassword(), user.getPassword());
        
        // 토큰 생성 및 응답 구성
        UserDto userDto = UserDto.fromEntity(user);
        String token = jwtTokenUtil.generateToken(userDto);
        
        log.info("사용자 로그인 성공: {}", request.getEmail());
        
        return buildLoginResponse(token, userDto);
    }
    
    /**
     * 이메일로 사용자를 조회하고, 없으면 예외를 발생시키는 메서드
     * 
     * @param email 조회할 이메일
     * @return 조회된 사용자 엔티티
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생
     */
    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
    
    /**
     * 비밀번호를 검증하는 메서드
     * 
     * @param rawPassword 입력받은 평문 비밀번호
     * @param encodedPassword 저장된 암호화된 비밀번호
     * @throws BadCredentialsException 비밀번호가 일치하지 않는 경우 발생
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }
    }
    
    /**
     * 로그인 응답 객체를 생성하는 메서드
     * 
     * @param token 생성된 JWT 토큰
     * @param userDto 사용자 정보 DTO
     * @return 로그인 응답 객체
     */
    private LoginResponse buildLoginResponse(String token, UserDto userDto) {
        return LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .token(token)
                .user(userDto)
                .build();
    }
    
    /**
     * 이메일로 사용자 정보 조회
     *
     * @param email 이메일
     * @return 사용자 정보 DTO (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::fromEntity);
    }
} 