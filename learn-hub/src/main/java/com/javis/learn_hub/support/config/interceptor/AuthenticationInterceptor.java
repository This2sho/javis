package com.javis.learn_hub.support.config.interceptor;

import com.javis.learn_hub.support.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token;
        try {
            token = jwtUtil.resolveToken(request);
        } catch (IllegalStateException e) {
            redirectToLogin(request, response);
            return false;
        }
        if (!jwtUtil.validateToken(token)) {
            redirectToLogin(request, response);
            return false;
        }
        request.setAttribute(jwtUtil.getAccessTokenName(), token);
        return true;
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI().startsWith("/api")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.sendRedirect("/login");
    }
}
