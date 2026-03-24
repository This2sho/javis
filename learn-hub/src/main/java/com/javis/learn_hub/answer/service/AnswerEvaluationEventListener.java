package com.javis.learn_hub.answer.service;

import com.javis.learn_hub.event.EvaluationCompletedEvent;
import com.javis.learn_hub.event.EvaluationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnswerEvaluationEventListener {

    private final AnswerCommandService answerCommandService;

    @EventListener
    public void onEvaluationFailed(EvaluationFailedEvent event) {
        log.info("채점 실패 이벤트 수신, 답변 실패 처리: answerId={}", event.answerId());
        answerCommandService.failEvaluation(event.answerId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEvaluationCompleted(EvaluationCompletedEvent event) {
        log.info("채점 완료 이벤트 수신, 답변 완료 처리: answerId={}", event.answerId());
        answerCommandService.successEvaluation(event.answerId());
    }
}
