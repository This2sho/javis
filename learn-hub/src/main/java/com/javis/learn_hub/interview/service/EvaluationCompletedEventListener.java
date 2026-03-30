package com.javis.learn_hub.interview.service;

import com.javis.learn_hub.event.EvaluationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationCompletedEventListener {

    private final InterviewFlowService interviewFlowService;

    @Async("nextQuestionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvaluationCompleted(EvaluationCompletedEvent event) {
        log.debug("채점 완료 이벤트 수신: answerId={}, questionId={}", event.answerId(), event.questionId());
        interviewFlowService.continueNextQuestion(event.questionId(), event.preferences());
    }
}
