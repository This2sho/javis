package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.event.AnswerCreatedEvent;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationEventListener {

    private final EvaluationService evaluationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        log.info("답변 생성 이벤트 수신, 채점 요청: answerId={}, questionId={}", event.answerId(), event.questionId());
        evaluationService.requestEvaluation(event.questionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvaluationRetry(EvaluationRetryEvent event) {
        log.info("재채점 요청: questionId={}", event.questionId());
        evaluationService.requestEvaluation(event.questionId());
    }
}
