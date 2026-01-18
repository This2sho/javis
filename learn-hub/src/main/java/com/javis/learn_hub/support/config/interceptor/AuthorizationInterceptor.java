package com.javis.learn_hub.support.config.interceptor;

import com.javis.learn_hub.member.domain.Role;
import com.javis.learn_hub.support.domain.RequireRole;
import com.javis.learn_hub.support.exception.NotFoundException;
import com.javis.learn_hub.support.exception.UnauthorizedException;
import com.javis.learn_hub.support.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole =
                handlerMethod.getMethodAnnotation(RequireRole.class);

        // 인가 필요 없는 메서드
        if (requireRole == null) {
            return true;
        }

        String token = (String) request.getAttribute(jwtUtil.getAccessTokenName());
        if (token == null) {
            throw new UnauthorizedException();
        }

        Role userRole = jwtUtil.extractRole(token);
        Role requiredRole = requireRole.value();

        if (!userRole.hasAuthority(requiredRole)) {
            throw new NotFoundException();
        }

        return true;
    }
}
