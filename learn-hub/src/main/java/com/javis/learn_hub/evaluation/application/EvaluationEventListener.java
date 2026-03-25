package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.answer.service.AnswerCommandService;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.AnswerCreatedEvent;
import com.javis.learn_hub.event.EvaluationFailedEvent;
import com.javis.learn_hub.event.EvaluationRetryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class EvaluationEventListener {

    private final AnswerCommandService answerCommandService;
    private final EvaluationService evaluationService;
    private final ApplicationEventPublisher eventPublisher;

    @Async("evaluationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnswerCreated(AnswerCreatedEvent event) {
        log.info("답변 생성 이벤트 수신, 채점 요청: answerId={}, questionId={}", event.answerId(), event.questionId());
        answerCommandService.prepareScoring(event.questionId())
                        .ifPresent(answer -> evaluate(answer, event.questionId()));
    }

    @Async("evaluationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvaluationRetry(EvaluationRetryEvent event) {
        log.info("재채점 요청: questionId={}", event.questionId());
        answerCommandService.prepareScoring(event.questionId())
                .ifPresent(answer -> evaluate(answer, event.questionId()));
    }

    private void evaluate(Answer answer, Long questionId) {
        try {
            log.info("채점 요청 전송: answerId={}", answer.getId());
            EvaluationResponse result = evaluationService.evaluate(answer, questionId);
            evaluationService.completeEvaluation(answer.getId(), questionId, result);
        } catch (Exception e) {
            eventPublisher.publishEvent(EvaluationFailedEvent.of(answer.getId(), questionId));
            log.error("채점 요청 최종 실패, 실패 이벤트 발행: answerId={}", answer.getId());
        }
    }
}
