package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.EvaluationCompletedEvent;
import com.javis.learn_hub.interview.service.dto.InterviewerResponse;
import com.javis.learn_hub.support.websocket.InterviewWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationCompletedEventListener {

    private final InterviewCommandService interviewCommandService;
    private final InterviewWebSocketHandler webSocketHandler;

    @EventListener
    public void onEvaluationCompleted(EvaluationCompletedEvent event) {
        log.info("채점 완료 이벤트 수신: answerId={}, questionId={}", event.answerId(), event.questionId());

        interviewCommandService.markQuestionCompleted(event.questionId());

        InterviewerResponse response = interviewCommandService.continueNextQuestion(
                event.questionId(),
                event.preferences()
        );

        webSocketHandler.sendNextQuestion(
                event.memberId(),
                response
        );
    }
}
