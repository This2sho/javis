package com.javis.learn_hub.support.websocket;

import com.javis.learn_hub.event.EvaluationFailedEvent;
import com.javis.learn_hub.interview.domain.service.InterviewReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationFailedEventListener {

    private final InterviewWebSocketHandler webSocketHandler;
    private final InterviewReader interviewReader;

    @EventListener
    public void onEvaluationFailed(EvaluationFailedEvent event) {
        Long memberId = interviewReader.getMemberIdByQuestionId(event.questionId());
        log.warn("채점 실패 이벤트 수신: questionId={}, memberId={}", event.questionId(), memberId);
        webSocketHandler.sendEvaluationFailed(memberId);
    }
}
