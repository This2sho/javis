package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.answer.domain.service.AnswerProcessor;
import com.javis.learn_hub.event.EvaluationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnswerEvaluationEventListener {

    private final AnswerProcessor answerProcessor;

    @EventListener
    public void onEvaluationFailed(EvaluationFailedEvent event) {
        log.info("채점 실패 이벤트 수신, 답변 실패 처리: answerId={}", event.answerId());
        answerProcessor.fail(event.answerId());
    }
}
