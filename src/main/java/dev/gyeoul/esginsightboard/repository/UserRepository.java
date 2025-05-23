package dev.gyeoul.esginsightboard.repository;

import dev.gyeoul.esginsightboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일이 이미 존재하는지 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
} 