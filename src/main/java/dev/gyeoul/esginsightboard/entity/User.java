package dev.gyeoul.esginsightboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 사용자 정보를 저장하는 엔티티
 * Spring Security의 UserDetails 인터페이스를 구현하여 인증 정보를 제공
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA에 필요한 기본 생성자를 protected로 제한
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String department;  // 부서

    @Column
    private String position;    // 직급/직책

    @Column
    private String companyName; // 회사명
    
    @Column
    private String ceoName;     // 대표자명
    
    @Column
    private String companyCode; // 회사 코드
    
    @Column
    private String companyPhoneNumber; // 회사 전화번호

    @Column
    private String phoneNumber; // 개인 전화번호

    /**
     * 사용자가 속한 회사
     * 여러 사용자가 하나의 회사에 소속될 수 있으므로 ManyToOne 관계로 설정
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "account_non_expired", columnDefinition = "boolean default true")
    private boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked", columnDefinition = "boolean default true")
    private boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired", columnDefinition = "boolean default true")
    private boolean credentialsNonExpired = true;
    
    @Column(columnDefinition = "boolean default true")
    private boolean enabled = true;

    /**
     * User 엔티티 생성을 위한 빌더 패턴 구현
     */
    @Builder
    public User(Long id, String email, String password, String name, String department, 
                String position, String companyName, String ceoName, String companyCode, 
                String companyPhoneNumber, String phoneNumber, Company company,
                boolean accountNonExpired, boolean accountNonLocked, 
                boolean credentialsNonExpired, boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.department = department;
        this.position = position;
        this.companyName = companyName;
        this.ceoName = ceoName;
        this.companyCode = companyCode;
        this.companyPhoneNumber = companyPhoneNumber;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    /**
     * 엔티티 생성 시 호출되어 생성 시간과 수정 시간을 현재 시간으로 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 수정 시 호출되어 수정 시간을 현재 시간으로 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 사용자의 권한 목록을 반환
     * 현재는 모든 사용자에게 ROLE_USER 권한만 부여
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    /**
     * 사용자의 식별자로 사용될 username을 반환 (Spring Security)
     * 이 애플리케이션에서는 email을 username으로 사용
     */
    @Override
    public String getUsername() {
        return this.email;
    }
    
    // Spring Security UserDetails 인터페이스 구현 메서드
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
} 