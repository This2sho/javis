package com.javis.learn_hub.evaluation.application;

import com.javis.learn_hub.answer.domain.Answer;
import com.javis.learn_hub.evaluation.domain.service.EvaluationProcessor;
import com.javis.learn_hub.evaluation.infrastructure.AnswerEvaluator;
import com.javis.learn_hub.evaluation.infrastructure.dto.EvaluationResponse;
import com.javis.learn_hub.event.EvaluationCompletedEvent;
import com.javis.learn_hub.problem.domain.ProblemScoringInfo;
import com.javis.learn_hub.problem.domain.service.ProblemReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EvaluationService {

    private final AnswerEvaluator answerEvaluator;
    private final EvaluationProcessor evaluationProcessor;
    private final ProblemReader problemReader;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluationResponse evaluate(Answer answer, Long questionId) {
        ProblemScoringInfo scoringInfo = problemReader.getProblemScoringInfoByQuestionId(questionId);
        return answerEvaluator.evaluate(scoringInfo.getReferenceAnswer(), answer.getMessage());
    }

    @Transactional
    public void completeEvaluation(Long answerId, Long questionId, EvaluationResponse result) {
        EvaluationCompletedEvent completedEvent = evaluationProcessor.complete(answerId, questionId, result);
        eventPublisher.publishEvent(completedEvent);
        log.info("채점 완료: answerId={}, grade={}", answerId, result.grade());
    }
}
