package com.javis.learn_hub.support.websocket;

import com.javis.learn_hub.support.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@RequiredArgsConstructor
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        String token;
        try {
            token = jwtUtil.resolveToken(servletRequest);
        } catch (IllegalStateException e) {
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        Long memberId = jwtUtil.getMemberId(token);
        attributes.put("memberId", memberId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
