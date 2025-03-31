package dev.gyeoul.esginsightboard.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 여부를 체크하는 인터셉터
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String USER_SESSION_KEY = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            // 미인증 사용자는 401 Unauthorized 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }
        
        return true;
    }
} 