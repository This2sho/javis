package com.javis.learn_hub.support.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(Long memberId, WebSocketSession session) {
        sessions.put(memberId, session);
        log.info("WebSocket 세션 등록: memberId={}, sessionId={}", memberId, session.getId());
    }

    public void removeSession(Long memberId) {
        WebSocketSession removed = sessions.remove(memberId);
        if (removed != null) {
            log.info("WebSocket 세션 제거: memberId={}", memberId);
        }
    }

    public WebSocketSession getSession(Long memberId) {
        return sessions.get(memberId);
    }
}
