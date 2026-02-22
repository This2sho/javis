package com.javis.learn_hub.support.websocket;

import com.javis.learn_hub.event.EvaluationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationFailedEventListener {

    private final InterviewWebSocketHandler webSocketHandler;

    @EventListener
    public void onEvaluationFailed(EvaluationFailedEvent event) {
        log.warn("채점 실패 이벤트 수신: questionId={}, memberId={}", event.questionId(), event.memberId());
        webSocketHandler.sendEvaluationFailed(event.memberId());
    }
}
