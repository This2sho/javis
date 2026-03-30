package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.EvaluationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EvaluationFacade {

    private final EvaluationService evaluationService;
    private final ApplicationEventPublisher eventPublisher;

    public void processEvaluation(Answer answer, Long questionId) {
        try {
            EvaluationResponse result = evaluationService.evaluate(answer, questionId);
            evaluationService.completeEvaluation(answer.getId(), questionId, result);
        } catch (Exception e) {
            eventPublisher.publishEvent(EvaluationFailedEvent.of(answer.getId(), questionId));
            log.error("채점 요청 최종 실패: answerId={}", answer.getId(), e);
        }
    }
}
