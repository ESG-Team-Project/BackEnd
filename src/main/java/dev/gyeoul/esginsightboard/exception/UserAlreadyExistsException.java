package dev.gyeoul.esginsightboard.exception;

/**
 * 이미 존재하는 사용자(이메일)로 회원가입을 시도할 때 발생하는 예외
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
} 