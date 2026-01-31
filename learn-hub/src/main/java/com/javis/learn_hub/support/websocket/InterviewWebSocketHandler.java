package com.javis.learn_hub.support.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.support.websocket.dto.InterviewProgressMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
@Component
public class InterviewWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long memberId = extractMemberId(session);
        if (memberId != null) {
            sessionManager.addSession(memberId, session);
            log.info("WebSocket 연결: memberId={}", memberId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long memberId = extractMemberId(session);
        if (memberId != null) {
            sessionManager.removeSession(memberId);
            log.info("WebSocket 종료: memberId={}", memberId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("WebSocket 메시지 수신: {}", message.getPayload());
    }

    public void sendNextQuestion(Long memberId, InterviewerResponse response) {
        WebSocketSession session = sessionManager.getSession(memberId);
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket 세션 없음 또는 닫힘: memberId={}", memberId);
            return;
        }

        try {
            InterviewProgressMessage message = new InterviewProgressMessage(
                    response.ended(),
                    response.interviewId(),
                    response.questionId(),
                    response.interviewerMessage()
            );
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.info("다음 질문 WebSocket 전송: memberId={}, ended={}", memberId, response.ended());
        } catch (IOException e) {
            log.error("WebSocket 메시지 전송 실패: memberId={}", memberId, e);
        }
    }

    private Long extractMemberId(WebSocketSession session) {
        return (Long) session.getAttributes().get("memberId");
    }
}
