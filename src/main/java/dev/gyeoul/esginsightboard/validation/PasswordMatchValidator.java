package dev.gyeoul.esginsightboard.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 비밀번호와 확인용 비밀번호가 일치하는지 검증하는 Validator
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String password;
    private String checkPassword;
    private String message;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.password = constraintAnnotation.password();
        this.checkPassword = constraintAnnotation.checkPassword();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object passwordValue = new BeanWrapperImpl(value).getPropertyValue(password);
        Object checkPasswordValue = new BeanWrapperImpl(value).getPropertyValue(checkPassword);
        
        boolean isValid = false;
        
        if (passwordValue != null) {
            isValid = passwordValue.equals(checkPasswordValue);
        }
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                   .addPropertyNode(checkPassword)
                   .addConstraintViolation();
        }
        
        return isValid;
    }
} 