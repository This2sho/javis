package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.EvaluationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationCompletedEventListener {

    private final NextQuestionService nextQuestionService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEvaluationCompleted(EvaluationCompletedEvent event) {
        log.info("채점 완료 이벤트 수신: answerId={}, questionId={}", event.answerId(), event.questionId());
        nextQuestionService.continueNextQuestion(event.questionId(), event.preferences());
    }
}
